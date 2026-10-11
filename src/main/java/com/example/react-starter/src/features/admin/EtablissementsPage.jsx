import { useEffect, useState } from 'react'
import { Building2, RotateCcw, Plus, Power, Pencil, X, BarChart3, Users } from 'lucide-react'
import { api } from '../../api/client.js'

const emptyAdmin = { username: '', password: '', nom: '', prenom: '', email: '' }
const emptyForm = { code: '', nom: '', statut: 'ACTIF', parametres: [], administrateur: emptyAdmin }

function toParamList(map) {
    return Object.entries(map || {}).map(([key, value]) => ({ key, value }))
}
function toParamMap(list) {
    return Object.fromEntries(list.filter(p => p.key.trim()).map(p => [p.key.trim(), p.value]))
}

export default function EtablissementsPage() {
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', etablissements: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const requestKey = String(retryVersion)
    const etablissements = result.key === requestKey ? result.etablissements : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    const [stats, setStats] = useState(null)
    const [statsError, setStatsError] = useState('')

    const [form, setForm] = useState(emptyForm)
    const [editingCode, setEditingCode] = useState(null)
    const [saving, setSaving] = useState(false)
    const [saveError, setSaveError] = useState('')
    const [saveSuccess, setSaveSuccess] = useState('')

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/etablissements', { signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, etablissements: data }))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setFailure({ key: requestKey, message: err.response?.data?.message || 'Impossible de charger les établissements.' })
            })
        return () => controller.abort()
    }, [requestKey])

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/etablissements/statistiques', { signal: controller.signal })
            .then(({ data }) => setStats(data))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setStatsError('Statistiques indisponibles.')
            })
        return () => controller.abort()
    }, [retryVersion])

    function startEdit(etab) {
        setEditingCode(etab.code)
        setForm({
            code: etab.code,
            nom: etab.nom,
            statut: etab.statut,
            parametres: toParamList(etab.parametres),
            administrateur: emptyAdmin,
        })
        setSaveError('')
        setSaveSuccess('')
    }

    function cancelEdit() {
        setEditingCode(null)
        setForm(emptyForm)
        setSaveError('')
    }

    function updateParam(index, field, value) {
        setForm(f => {
            const parametres = [...f.parametres]
            parametres[index] = { ...parametres[index], [field]: value }
            return { ...f, parametres }
        })
    }
    function addParam() {
        setForm(f => ({ ...f, parametres: [...f.parametres, { key: '', value: '' }] }))
    }
    function removeParam(index) {
        setForm(f => ({ ...f, parametres: f.parametres.filter((_, i) => i !== index) }))
    }

    async function handleSubmit(event) {
        event.preventDefault()
        setSaveError('')
        setSaveSuccess('')

        if (!form.code.trim() || !form.nom.trim()) {
            setSaveError('Le code et le nom sont obligatoires.')
            return
        }
        if (!editingCode) {
            const a = form.administrateur
            if (!a.username.trim() || !a.password.trim() || !a.nom.trim() || !a.prenom.trim() || !a.email.trim()) {
                setSaveError('Tous les champs de l\'administrateur sont obligatoires à la création.')
                return
            }
            if (a.password.length < 8) {
                setSaveError('Le mot de passe de l\'administrateur doit faire au moins 8 caractères.')
                return
            }
        }

        const payload = {
            code: form.code.trim(),
            nom: form.nom.trim(),
            statut: form.statut,
            parametres: toParamMap(form.parametres),
            administrateur: editingCode ? null : form.administrateur,
        }

        setSaving(true)
        try {
            if (editingCode) {
                await api.put(`/api/etablissements/${editingCode}`, payload)
                setSaveSuccess('Établissement modifié avec succès.')
            } else {
                await api.post('/api/etablissements', payload)
                setSaveSuccess('Établissement créé avec succès.')
            }
            setForm(emptyForm)
            setEditingCode(null)
            setRetryVersion(v => v + 1)
        } catch (err) {
            setSaveError(err.response?.data?.message || 'Impossible d\'enregistrer l\'établissement.')
        } finally {
            setSaving(false)
        }
    }

    async function handleToggle(code, statut) {
        const nouveauStatut = statut === 'ACTIF' ? 'INACTIF' : 'ACTIF'
        try {
            await api.patch(`/api/etablissements/${code}/statut`, null, { params: { valeur: nouveauStatut } })
            setRetryVersion(v => v + 1)
        } catch (err) {
            alert(err.response?.data?.message || 'Impossible de modifier le statut.')
        }
    }

    return <section className="content etablissements-page">
        <div className="page-heading">
            <span className="eyebrow">ADMINISTRATION DE LA PLATEFORME</span>
            <h1>Établissements</h1>
            <p>Gérez les établissements hébergés sur BiblioUniv.</p>
        </div>

        {stats && !statsError && (
            <div className="stats-cards">
                <div className="stat-card"><BarChart3 size={18} /><span>{stats.etablissements}</span><small>Établissements</small></div>
                <div className="stat-card"><Power size={18} /><span>{stats.etablissementsActifs}</span><small>Actifs</small></div>
                <div className="stat-card"><Users size={18} /><span>{stats.utilisateurs}</span><small>Utilisateurs</small></div>
                <div className="stat-card"><Building2 size={18} /><span>{stats.ressources}</span><small>Ressources</small></div>
            </div>
        )}

        <div className="notification-form-card">
            <h2>{editingCode ? `Modifier ${editingCode}` : 'Nouvel établissement'}</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="code">Code unique</label>
                    <input
                        id="code" type="text" placeholder="Ex : ucad"
                        value={form.code}
                        onChange={e => setForm(f => ({ ...f, code: e.target.value }))}
                        disabled={saving || !!editingCode}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="nom">Nom de l'établissement</label>
                    <input
                        id="nom" type="text" placeholder="Ex : Université Cheikh Anta Diop"
                        value={form.nom}
                        onChange={e => setForm(f => ({ ...f, nom: e.target.value }))}
                        disabled={saving}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="statut">Statut</label>
                    <select
                        id="statut"
                        value={form.statut}
                        onChange={e => setForm(f => ({ ...f, statut: e.target.value }))}
                        disabled={saving}
                    >
                        <option value="ACTIF">Actif</option>
                        <option value="INACTIF">Inactif</option>
                    </select>
                </div>

                <div className="form-group">
                    <label>Paramètres</label>
                    {form.parametres.map((p, i) => (
                        <div key={i} className="param-row">
                            <input type="text" placeholder="Clé" value={p.key} onChange={e => updateParam(i, 'key', e.target.value)} disabled={saving} />
                            <input type="text" placeholder="Valeur" value={p.value} onChange={e => updateParam(i, 'value', e.target.value)} disabled={saving} />
                            <button type="button" className="icon-button" onClick={() => removeParam(i)} aria-label="Retirer"><X size={14} /></button>
                        </div>
                    ))}
                    <button type="button" className="secondary-button" onClick={addParam} disabled={saving}>
                        <Plus size={14} />Ajouter un paramètre
                    </button>
                </div>

                {!editingCode && (
                    <fieldset className="admin-fieldset">
                        <legend>Premier administrateur</legend>
                        <div className="form-group">
                            <label htmlFor="adminUsername">Identifiant</label>
                            <input id="adminUsername" type="text" value={form.administrateur.username}
                                   onChange={e => setForm(f => ({ ...f, administrateur: { ...f.administrateur, username: e.target.value } }))}
                                   disabled={saving} required />
                        </div>
                        <div className="form-group">
                            <label htmlFor="adminPassword">Mot de passe</label>
                            <input id="adminPassword" type="password" value={form.administrateur.password}
                                   onChange={e => setForm(f => ({ ...f, administrateur: { ...f.administrateur, password: e.target.value } }))}
                                   disabled={saving} required minLength={8} />
                        </div>
                        <div className="form-group">
                            <label htmlFor="adminNom">Nom</label>
                            <input id="adminNom" type="text" value={form.administrateur.nom}
                                   onChange={e => setForm(f => ({ ...f, administrateur: { ...f.administrateur, nom: e.target.value } }))}
                                   disabled={saving} required />
                        </div>
                        <div className="form-group">
                            <label htmlFor="adminPrenom">Prénom</label>
                            <input id="adminPrenom" type="text" value={form.administrateur.prenom}
                                   onChange={e => setForm(f => ({ ...f, administrateur: { ...f.administrateur, prenom: e.target.value } }))}
                                   disabled={saving} required />
                        </div>
                        <div className="form-group">
                            <label htmlFor="adminEmail">Email</label>
                            <input id="adminEmail" type="email" value={form.administrateur.email}
                                   onChange={e => setForm(f => ({ ...f, administrateur: { ...f.administrateur, email: e.target.value } }))}
                                   disabled={saving} required />
                        </div>
                    </fieldset>
                )}

                {saveError && <p className="form-notice" role="alert">{saveError}</p>}
                {saveSuccess && <p className="upload-message" role="status">{saveSuccess}</p>}
                <div className="form-actions">
                    <button className="primary-button" type="submit" disabled={saving}>
                        {editingCode ? <Pencil size={16} /> : <Plus size={16} />}
                        {saving ? 'Enregistrement...' : editingCode ? 'Modifier' : 'Créer'}
                    </button>
                    {editingCode && (
                        <button type="button" className="secondary-button" onClick={cancelEdit} disabled={saving}>
                            <X size={15} />Annuler
                        </button>
                    )}
                </div>
            </form>
        </div>

        <div className="notifications-list-section">
            <h2>Établissements hébergés</h2>

            {loading && <div className="catalog-state" role="status">Chargement des établissements...</div>}

            {!loading && error && (
                <div className="catalog-state catalog-state-error" role="alert">
                    <p>{error}</p>
                    <button className="secondary-button" onClick={() => setRetryVersion(v => v + 1)}>
                        <RotateCcw size={15} />Réessayer
                    </button>
                </div>
            )}

            {!loading && !error && etablissements.length === 0 && (
                <div className="catalog-state">
                    <Building2 size={24} />
                    <h2>Aucun établissement</h2>
                    <p>Aucun établissement n'est encore hébergé sur la plateforme.</p>
                </div>
            )}

            {!loading && !error && etablissements.length > 0 && (
                <div className="emprunts-table">
                    <table>
                        <thead>
                        <tr><th>Nom</th><th>Code</th><th>Statut</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                        {etablissements.map(etab => (
                            <tr key={etab.code}>
                                <td>{etab.nom}</td>
                                <td><code>{etab.code}</code></td>
                                <td>
                                    <span className={`badge badge--${etab.statut === 'ACTIF' ? 'actif' : 'inactif'}`}>
                                        {etab.statut === 'ACTIF' ? 'Actif' : 'Inactif'}
                                    </span>
                                </td>
                                <td className="table-actions">
                                    <button className="icon-button" onClick={() => startEdit(etab)} aria-label="Modifier">
                                        <Pencil size={15} />
                                    </button>
                                    <button className="secondary-button" onClick={() => handleToggle(etab.code, etab.statut)}>
                                        <Power size={15} />{etab.statut === 'ACTIF' ? 'Désactiver' : 'Activer'}
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    </section>
}