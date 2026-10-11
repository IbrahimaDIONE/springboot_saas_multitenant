import { useEffect, useState } from 'react'
import { Bell, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'

const notificationFilters = [
    { value: 'TOUTES', label: 'Toutes' },
    { value: 'NOUVELLE_RESSOURCE', label: 'Nouvelles ressources' },
    { value: 'RAPPEL_ECHEANCE', label: 'Rappels' },
    { value: 'AVERTISSEMENT_RETARD', label: 'Avertissements' },
    { value: 'PENALITE', label: 'Pénalités' },
]

export default function NotificationsAdminPage() {
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', notifications: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const requestKey = String(retryVersion)
    const notifications = result.key === requestKey ? result.notifications : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''
    const [typeFilter, setTypeFilter] = useState('TOUTES')
    const filteredNotifications = typeFilter === 'TOUTES'
        ? notifications
        : notifications.filter((notification) => notification.type === typeFilter)

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

    return <section className="content admin-page notifications-admin-page">
        <header className="admin-heading">
            <span className="eyebrow">ADMINISTRATION DE L'ETABLISSEMENT</span>
            <h1>Notifications</h1>
            <p>Consultez les notifications générées automatiquement pour les étudiants de l’établissement.</p>
        </header>

        <div className="notifications-list-section">
            <h2>Historique des notifications</h2>

            <div className="admin-toolbar notifications-toolbar">
                <div className="admin-tabs" role="group" aria-label="Filtrer les notifications par type">
                    {notificationFilters.map((filter) => (
                        <button
                            className={typeFilter === filter.value ? 'selected' : ''}
                            key={filter.value}
                            onClick={() => setTypeFilter(filter.value)}
                            type="button"
                        >
                            {filter.label}
                        </button>
                    ))}
                </div>
                <span className="notification-count">{filteredNotifications.length} notification(s)</span>
            </div>

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
                    <p>Les rappels et événements automatiques apparaîtront ici.</p>
                </div>
            )}

            {!loading && !error && notifications.length > 0 && filteredNotifications.length === 0 && (
                <div className="catalog-state">
                    <Bell size={24} />
                    <h2>Aucun résultat</h2>
                    <p>Aucune notification ne correspond à ce type.</p>
                </div>
            )}

            {!loading && !error && filteredNotifications.length > 0 && (
                <div className="emprunts-table">
                    <table>
                        <thead>
                        <tr>
                            <th>Étudiant</th>
                            <th>Type</th>
                            <th>Message</th>
                            <th>Lu</th>
                            <th>Date</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredNotifications.map(notif => (
                            <tr key={notif.id}>
                                <td data-label="Étudiant">{notif.etudiantId}</td>
                                <td data-label="Type">
                    <span className={`badge badge--${notif.type.toLowerCase()}`}>
                      {notif.type}
                    </span>
                                </td>
                                <td data-label="Message">{notif.message}</td>
                                <td data-label="Lu">{notif.lu ? 'Oui' : 'Non'}</td>
                                <td data-label="Date">{new Date(notif.createdAt).toLocaleDateString('fr-FR')}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    </section>
}