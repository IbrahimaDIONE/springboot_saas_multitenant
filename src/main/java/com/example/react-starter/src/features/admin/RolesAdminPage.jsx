import { useEffect, useState } from 'react'
import { Users, RotateCcw, ShieldCheck } from 'lucide-react'
import { api } from '../../api/client.js'

export default function RolesAdminPage() {
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', users: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const requestKey = String(retryVersion)
    const users = result.key === requestKey ? result.users : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    const [saving, setSaving] = useState(null)
    const [saveError, setSaveError] = useState('')

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/utilisateurs', { signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, users: data }))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setFailure({ key: requestKey, message: err.response?.data?.message || 'Impossible de charger les utilisateurs.' })
            })
        return () => controller.abort()
    }, [requestKey])

    async function handleChangeRole(userId, newRole) {
        setSaveError('')
        setSaving(userId)
        try {
            await api.put(`/api/utilisateurs/${userId}/role`, { role: newRole })
            setRetryVersion(v => v + 1)
        } catch (err) {
            setSaveError(err.response?.data?.message || 'Impossible de modifier le role.')
        } finally {
            setSaving(null)
        }
    }

    return <section className="content roles-admin-page">
        <div className="page-heading">
            <span className="eyebrow">ADMINISTRATION DE LA PLATEFORME</span>
            <h1>Gestion des roles</h1>
            <p>Modifiez le role des utilisateurs de la plateforme.</p>
        </div>

        {saveError && (
            <p className="form-notice" role="alert">{saveError}</p>
        )}

        {loading && (
            <div className="catalog-state" role="status">
                Chargement des utilisateurs...
            </div>
        )}

        {!loading && error && (
            <div className="catalog-state catalog-state-error" role="alert">
                <p>{error}</p>
                <button className="secondary-button" onClick={() => setRetryVersion(v => v + 1)}>
                    <RotateCcw size={15} />Reessayer
                </button>
            </div>
        )}

        {!loading && !error && users.length === 0 && (
            <div className="catalog-state">
                <Users size={24} />
                <h2>Aucun utilisateur</h2>
                <p>Aucun utilisateur n'est encore enregistre sur la plateforme.</p>
            </div>
        )}

        {!loading && !error && users.length > 0 && (
            <div className="emprunts-table">
                <table>
                    <thead>
                    <tr>
                        <th>Identifiant</th>
                        <th>Etablissement</th>
                        <th>Role actuel</th>
                        <th>Changer le role</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map(user => (
                        <tr key={user.id}>
                            <td>{user.username}</td>
                            <td>{user.tenantId || '—'}</td>
                            <td>
                  <span className={`badge badge--${user.role.toLowerCase()}`}>
                    <ShieldCheck size={13} />{user.role}
                  </span>
                            </td>
                            <td>
                                <select
                                    value={user.role}
                                    onChange={e => handleChangeRole(user.id, e.target.value)}
                                    disabled={saving === user.id}
                                    aria-label={`Changer le role de ${user.username}`}
                                >
                                    <option value="ETUDIANT">Etudiant</option>
                                    <option value="ADMIN_ETABLISSEMENT">Admin etablissement</option>
                                    <option value="ADMIN_PLATEFORME">Admin plateforme</option>
                                </select>
                                {saving === user.id && (
                                    <span className="catalog-state" role="status" style={{ fontSize: '12px' }}>
                      Enregistrement...
                    </span>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        )}
    </section>
}