import { useEffect, useState } from 'react'
import { ShieldAlert, RotateCcw, Plus, Pencil, Trash2, X } from 'lucide-react'
import { api } from '../../api/client.js'

const emptyForm = { joursTolérance: '', typeConsequence: '', description: '' }

export default function ReglesPenaliteAdminPage() {
    // Règles de pénalité
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', regles: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const requestKey = String(retryVersion)
    const regles = result.key === requestKey ? result.regles : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    // Formulaire création / édition
    const [form, setForm] = useState(emptyForm)
    const [editingId, setEditingId] = useState(null)
    const [saving, setSaving] = useState(false)
    const [formError, setFormError] = useState('')
    const [formSuccess, setFormSuccess] = useState('')

    // Pénalités appliquées
    const [penalites, setPenalites] = useState([])
    const [penalitesLoading, setPenalitesLoading] = useState(true)
    const [penalitesError, setPenalitesError] = useState('')
    const [penalitesVersion, setPenalitesVersion] = useState(0)

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/regles-penalite', { signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, regles: data }))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setFailure({ key: requestKey, message: err.response?.data?.message || 'Impossible de charger les règles de pénalité.' })
            })
        return () => controller.abort()
    }, [requestKey])

    useEffect(() => {
        const controller = new AbortController()
        setPenalitesLoading(true)
        setPenalitesError('')
        api.get('/api/penalites', { signal: controller.signal })
            .then(({ data }) => setPenalites(data))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setPenalitesError(err.response?.data?.message || 'Impossible de charger les pénalités appliquées.')
            })
            .finally(() => setPenalitesLoading(false))
        return () => controller.abort()
    }, [penalitesVersion])

    function startEdit(regle) {
        setEditingId(regle.id)
        setForm({
            joursTolérance: String(regle.joursTolérance),
            typeConsequence: regle.typeConsequence,
            description: regle.description || '',
        })
        setFormError('')
        setFormSuccess('')
    }

    function cancelEdit() {
        setEditingId(null)
        setForm(emptyForm)
        setFormError('')
    }

    async function handleSubmit(event) {
        event.preventDefault()
        setFormError('')
        setFormSuccess('')

        const jours = Number(form.joursTolérance)
        if (form.joursTolérance === '' || Number.isNaN(jours) || jours < 0) {
            setFormError('Le nombre de jours de tolérance doit être un nombre positif.')
            return
        }
        if (!form.typeConsequence.trim()) {
            setFormError('Le type de conséquence est obligatoire.')
            return
        }

        const payload = {
            joursTolérance: jours,
            typeConsequence: form.typeConsequence.trim(),
            description: form.description.trim(),
        }

        setSaving(true)
        try {
            if (editingId) {
                await api.put(`/api/regles-penalite/${editingId}`, payload)
                setFormSuccess('Règle modifiée avec succès.')
            } else {
                await api.post('/api/regles-penalite', payload)
                setFormSuccess('Règle créée avec succès.')
            }
            setForm(emptyForm)
            setEditingId(null)
            setRetryVersion(v => v + 1)
        } catch (err) {
            setFormError(err.response?.data?.message || 'Impossible d\'enregistrer la règle.')
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id) {
        if (!window.confirm('Supprimer cette règle de pénalité ?')) return
        try {
            await api.delete(`/api/regles-penalite/${id}`)
            setRetryVersion(v => v + 1)
            if (editingId === id) cancelEdit()
        } catch (err) {
            setFormError(err.response?.data?.message || 'Impossible de supprimer la règle.')
        }
    }

    return <section className="content regles-penalite-admin-page">
        <div className="page-heading">
            <span className="eyebrow">ADMINISTRATION DE L'ETABLISSEMENT</span>
            <h1>Règles de pénalité</h1>
            <p>Définissez les règles appliquées en cas de retard, et consultez les pénalités déjà appliquées.</p>
        </div>

        <div className="notification-form-card">
            <h2>{editingId ? 'Modifier la règle' : 'Créer une règle'}</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="joursTolerance">Jours de tolérance</label>
                    <input
                        id="joursTolerance"
                        type="number"
                        min="0"
                        placeholder="Ex : 3"
                        value={form.joursTolérance}
                        onChange={e => setForm(f => ({ ...f, joursTolérance: e.target.value }))}
                        disabled={saving}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="typeConsequence">Type de conséquence</label>
                    <input
                        id="typeConsequence"
                        type="text"
                        placeholder="Ex : AVERTISSEMENT, SUSPENSION"
                        value={form.typeConsequence}
                        onChange={e => setForm(f => ({ ...f, typeConsequence: e.target.value }))}
                        disabled={saving}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="description">Description</label>
                    <textarea
                        id="description"
                        placeholder="Détail de la règle (optionnel)"
                        value={form.description}
                        onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                        disabled={saving}
                        rows={3}
                    />
                </div>
                {formError && <p className="form-notice" role="alert">{formError}</p>}
                {formSuccess && <p className="upload-message" role="status">{formSuccess}</p>}
                <div className="form-actions">
                    <button className="primary-button" type="submit" disabled={saving}>
                        {editingId ? <Pencil size={16} /> : <Plus size={16} />}
                        {saving ? 'Enregistrement...' : editingId ? 'Modifier' : 'Créer'}
                    </button>
                    {editingId && (
                        <button type="button" className="secondary-button" onClick={cancelEdit} disabled={saving}>
                            <X size={15} />Annuler
                        </button>
                    )}
                </div>
            </form>
        </div>

        <div className="notifications-list-section">
            <h2>Règles existantes</h2>

            {loading && (
                <div className="catalog-state" role="status">
                    Chargement des règles de pénalité...
                </div>
            )}

            {!loading && error && (
                <div className="catalog-state catalog-state-error" role="alert">
                    <p>{error}</p>
                    <button className="secondary-button" onClick={() => setRetryVersion(v => v + 1)}>
                        <RotateCcw size={15} />Réessayer
                    </button>
                </div>
            )}

            {!loading && !error && regles.length === 0 && (
                <div className="catalog-state">
                    <ShieldAlert size={24} />
                    <h2>Aucune règle définie</h2>
                    <p>Créez une première règle de pénalité ci-dessus.</p>
                </div>
            )}

            {!loading && !error && regles.length > 0 && (
                <div className="emprunts-table">
                    <table>
                        <thead>
                        <tr>
                            <th>Jours de tolérance</th>
                            <th>Conséquence</th>
                            <th>Description</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {regles.map(regle => (
                            <tr key={regle.id}>
                                <td>{regle.joursTolérance}</td>
                                <td><span className="badge">{regle.typeConsequence}</span></td>
                                <td>{regle.description || '—'}</td>
                                <td className="table-actions">
                                    <button className="icon-button" onClick={() => startEdit(regle)} aria-label="Modifier">
                                        <Pencil size={15} />
                                    </button>
                                    <button className="icon-button" onClick={() => handleDelete(regle.id)} aria-label="Supprimer">
                                        <Trash2 size={15} />
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>

        <div className="notifications-list-section">
            <h2>Pénalités appliquées</h2>

            {penalitesLoading && (
                <div className="catalog-state" role="status">
                    Chargement des pénalités...
                </div>
            )}

            {!penalitesLoading && penalitesError && (
                <div className="catalog-state catalog-state-error" role="alert">
                    <p>{penalitesError}</p>
                    <button className="secondary-button" onClick={() => setPenalitesVersion(v => v + 1)}>
                        <RotateCcw size={15} />Réessayer
                    </button>
                </div>
            )}

            {!penalitesLoading && !penalitesError && penalites.length === 0 && (
                <div className="catalog-state">
                    <ShieldAlert size={24} />
                    <h2>Aucune pénalité</h2>
                    <p>Aucune pénalité n'a encore été appliquée.</p>
                </div>
            )}

            {!penalitesLoading && !penalitesError && penalites.length > 0 && (
                <div className="emprunts-table">
                    <table>
                        <thead>
                        <tr>
                            <th>Étudiant</th>
                            <th>Emprunt</th>
                            <th>Motif</th>
                            <th>Conséquence</th>
                            <th>Date</th>
                        </tr>
                        </thead>
                        <tbody>
                        {penalites.map(p => (
                            <tr key={p.id}>
                                <td>{p.etudiantId}</td>
                                <td>{p.empruntId}</td>
                                <td>{p.motif}</td>
                                <td><span className="badge">{p.consequence}</span></td>
                                <td>{new Date(p.dateApplication).toLocaleDateString('fr-FR')}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    </section>
}