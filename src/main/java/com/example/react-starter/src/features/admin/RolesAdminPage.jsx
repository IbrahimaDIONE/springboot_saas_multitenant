import { useEffect, useState } from 'react'
import { ShieldCheck, RotateCcw, Plus, Pencil, Trash2, X } from 'lucide-react'
import { api } from '../../api/client.js'

const emptyForm = { code: '', description: '', actif: true }

export default function RolesAdminPage() {
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', roles: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const requestKey = String(retryVersion)
    const roles = result.key === requestKey ? result.roles : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    const [form, setForm] = useState(emptyForm)
    const [editingId, setEditingId] = useState(null)
    const [saving, setSaving] = useState(false)
    const [saveError, setSaveError] = useState('')
    const [saveSuccess, setSaveSuccess] = useState('')

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/roles', { signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, roles: data }))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setFailure({ key: requestKey, message: err.response?.data?.message || 'Impossible de charger les rôles.' })
            })
        return () => controller.abort()
    }, [requestKey])

    function startEdit(role) {
        setEditingId(role.id)
        setForm({ code: role.code, description: role.description || '', actif: role.actif })
        setSaveError('')
        setSaveSuccess('')
    }

    function cancelEdit() {
        setEditingId(null)
        setForm(emptyForm)
        setSaveError('')
    }

    async function handleSubmit(event) {
        event.preventDefault()
        setSaveError('')
        setSaveSuccess('')
        if (!form.code.trim()) {
            setSaveError('Le code du rôle est obligatoire.')
            return
        }
        const payload = { code: form.code.trim(), description: form.description.trim(), actif: form.actif }

        setSaving(true)
        try {
            if (editingId) {
                await api.put(`/api/roles/${editingId}`, payload)
                setSaveSuccess('Rôle modifié avec succès.')
            } else {
                await api.post('/api/roles', payload)
                setSaveSuccess('Rôle créé avec succès.')
            }
            setForm(emptyForm)
            setEditingId(null)
            setRetryVersion(v => v + 1)
        } catch (err) {
            setSaveError(err.response?.data?.message || 'Impossible d\'enregistrer le rôle.')
        } finally {
            setSaving(false)
        }
    }

    async function handleDelete(id) {
        if (!window.confirm('Supprimer ce rôle ?')) return
        try {
            await api.delete(`/api/roles/${id}`)
            setRetryVersion(v => v + 1)
            if (editingId === id) cancelEdit()
        } catch (err) {
            alert(err.response?.data?.message || 'Impossible de supprimer ce rôle.')
        }
    }

    return <section className="content roles-admin-page">
        <div className="page-heading">
            <span className="eyebrow">ADMINISTRATION DE LA PLATEFORME</span>
            <h1>Rôles</h1>
            <p>Gérez les rôles disponibles sur la plateforme.</p>
        </div>

        <div className="notification-form-card">
            <h2>{editingId ? 'Modifier le rôle' : 'Nouveau rôle'}</h2>
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="code">Code</label>
                    <input
                        id="code" type="text" placeholder="Ex : ADMIN_ETABLISSEMENT"
                        value={form.code}
                        onChange={e => setForm(f => ({ ...f, code: e.target.value }))}
                        disabled={saving}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="description">Description</label>
                    <textarea
                        id="description" placeholder="Description du rôle (optionnel)"
                        value={form.description}
                        onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                        disabled={saving}
                        rows={3}
                    />
                </div>
                <div className="form-group form-group--checkbox">
                    <label htmlFor="actif">
                        <input
                            id="actif" type="checkbox"
                            checked={form.actif}
                            onChange={e => setForm(f => ({ ...f, actif: e.target.checked }))}
                            disabled={saving}
                        />
                        Rôle actif
                    </label>
                </div>
                {saveError && <p className="form-notice" role="alert">{saveError}</p>}
                {saveSuccess && <p className="upload-message" role="status">{saveSuccess}</p>}
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
            <h2>Rôles existants</h2>

            {loading && <div className="catalog-state" role="status">Chargement des rôles...</div>}

            {!loading && error && (
                <div className="catalog-state catalog-state-error" role="alert">
                    <p>{error}</p>
                    <button className="secondary-button" onClick={() => setRetryVersion(v => v + 1)}>
                        <RotateCcw size={15} />Réessayer
                    </button>
                </div>
            )}

            {!loading && !error && roles.length === 0 && (
                <div className="catalog-state">
                    <ShieldCheck size={24} />
                    <h2>Aucun rôle</h2>
                    <p>Aucun rôle n'est encore défini sur la plateforme.</p>
                </div>
            )}

            {!loading && !error && roles.length > 0 && (
                <div className="emprunts-table">
                    <table>
                        <thead>
                        <tr><th>Code</th><th>Description</th><th>Statut</th><th>Actions</th></tr>
                        </thead>
                        <tbody>
                        {roles.map(role => (
                            <tr key={role.id}>
                                <td><span className="badge">{role.code}</span></td>
                                <td>{role.description || '—'}</td>
                                <td>
                                    <span className={`badge badge--${role.actif ? 'actif' : 'inactif'}`}>
                                        {role.actif ? 'Actif' : 'Inactif'}
                                    </span>
                                </td>
                                <td className="table-actions">
                                    <button className="icon-button" onClick={() => startEdit(role)} aria-label="Modifier">
                                        <Pencil size={15} />
                                    </button>
                                    <button className="icon-button" onClick={() => handleDelete(role.id)} aria-label="Supprimer">
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
    </section>
}