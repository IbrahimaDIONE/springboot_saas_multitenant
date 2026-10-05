import { useEffect, useState } from 'react'
import { Bell, RotateCcw, Send } from 'lucide-react'
import { api } from '../../api/client.js'

export default function NotificationsAdminPage() {
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', notifications: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const requestKey = String(retryVersion)
    const notifications = result.key === requestKey ? result.notifications : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    const [form, setForm] = useState({ etudiantId: '', message: '', type: 'NOUVELLE_RESSOURCE' })
    const [sending, setSending] = useState(false)
    const [sendError, setSendError] = useState('')
    const [sendSuccess, setSendSuccess] = useState('')

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/notifications', { signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, notifications: data }))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setFailure({ key: requestKey, message: err.response?.data?.message || 'Impossible de charger les notifications.' })
            })
        return () => controller.abort()
    }, [requestKey])

    async function handleEnvoyer(event) {
        event.preventDefault()
        setSendError('')
        setSendSuccess('')
        if (!form.etudiantId.trim() || !form.message.trim()) {
            setSendError('Veuillez remplir tous les champs obligatoires.')
            return
        }
        setSending(true)
        try {
            await api.post('/api/notifications', form)
            setSendSuccess('Notification envoyee avec succes.')
            setForm({ etudiantId: '', message: '', type: 'NOUVELLE_RESSOURCE' })
            setRetryVersion(v => v + 1)
        } catch (err) {
            setSendError(err.response?.data?.message || 'Impossible d\'envoyer la notification.')
        } finally {
            setSending(false)
        }
    }

    return <section className="content notifications-admin-page">
        <div className="page-heading">
            <span className="eyebrow">ADMINISTRATION DE L'ETABLISSEMENT</span>
            <h1>Notifications</h1>
            <p>Envoyez des notifications aux etudiants et consultez l'historique.</p>
        </div>

        <div className="notification-form-card">
            <h2>Envoyer une notification</h2>
            <form onSubmit={handleEnvoyer}>
                <div className="form-group">
                    <label htmlFor="etudiantId">Identifiant etudiant</label>
                    <input
                        id="etudiantId"
                        type="text"
                        placeholder="UUID de l'etudiant"
                        value={form.etudiantId}
                        onChange={e => setForm(f => ({ ...f, etudiantId: e.target.value }))}
                        disabled={sending}
                        required
                    />
                </div>
                <div className="form-group">
                    <label htmlFor="type">Type</label>
                    <select
                        id="type"
                        value={form.type}
                        onChange={e => setForm(f => ({ ...f, type: e.target.value }))}
                        disabled={sending}
                    >
                        <option value="NOUVELLE_RESSOURCE">Nouvelle ressource</option>
                        <option value="RAPPEL_ECHEANCE">Rappel echeance</option>
                        <option value="AVERTISSEMENT_RETARD">Avertissement retard</option>
                        <option value="PENALITE">Penalite</option>
                    </select>
                </div>
                <div className="form-group">
                    <label htmlFor="message">Message</label>
                    <textarea
                        id="message"
                        placeholder="Votre message..."
                        value={form.message}
                        onChange={e => setForm(f => ({ ...f, message: e.target.value }))}
                        disabled={sending}
                        required
                        rows={3}
                    />
                </div>
                {sendError && <p className="form-notice" role="alert">{sendError}</p>}
                {sendSuccess && <p className="upload-message" role="status">{sendSuccess}</p>}
                <button className="primary-button" type="submit" disabled={sending}>
                    <Send size={16} />{sending ? 'Envoi...' : 'Envoyer'}
                </button>
            </form>
        </div>

        <div className="notifications-list-section">
            <h2>Historique des notifications</h2>

            {loading && (
                <div className="catalog-state" role="status">
                    Chargement des notifications...
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

            {!loading && !error && notifications.length === 0 && (
                <div className="catalog-state">
                    <Bell size={24} />
                    <h2>Aucune notification</h2>
                    <p>Aucune notification n'a encore ete envoyee.</p>
                </div>
            )}

            {!loading && !error && notifications.length > 0 && (
                <div className="emprunts-table">
                    <table>
                        <thead>
                        <tr>
                            <th>Etudiant</th>
                            <th>Type</th>
                            <th>Message</th>
                            <th>Lu</th>
                            <th>Date</th>
                        </tr>
                        </thead>
                        <tbody>
                        {notifications.map(notif => (
                            <tr key={notif.id}>
                                <td>{notif.etudiantId}</td>
                                <td>
                    <span className={`badge badge--${notif.type.toLowerCase()}`}>
                      {notif.type}
                    </span>
                                </td>
                                <td>{notif.message}</td>
                                <td>{notif.lu ? 'Oui' : 'Non'}</td>
                                <td>{new Date(notif.createdAt).toLocaleDateString('fr-FR')}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    </section>
}