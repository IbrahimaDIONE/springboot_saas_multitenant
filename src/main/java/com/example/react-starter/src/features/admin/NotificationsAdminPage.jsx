import { Fragment, useEffect, useMemo, useState } from 'react'
import { Bell, ChevronDown, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'
import { PaginationControls } from '../../components/Pagination.jsx'
import { usePagination } from '../../components/usePagination.js'

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
    const notificationGroups = useMemo(() => {
        const groups = new Map()
        for (const notification of filteredNotifications) {
            const key = notification.groupId || notification.id
            const group = groups.get(key)
            if (group) group.push(notification)
            else groups.set(key, [notification])
        }
        return Array.from(groups, ([id, deliveries]) => ({
            id,
            deliveries,
            first: deliveries[0],
            readCount: deliveries.filter((delivery) => delivery.lu).length,
        }))
    }, [filteredNotifications])
    const readCount = notifications.filter((notification) => notification.lu).length
    const unreadCount = notifications.length - readCount
    const [expandedGroupId, setExpandedGroupId] = useState(null)
    const notificationPage = usePagination(notificationGroups)

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
                <span className="notification-count">{notificationGroups.length} notification(s)</span>
            </div>
            <p className="notification-count" role="status">
                {readCount} lue(s) · {unreadCount} non lue(s) sur {notifications.length} notification(s) envoyée(s)
            </p>

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
                            <th>Type</th>
                            <th>Message</th>
                            <th>Destinataires</th>
                            <th>Date d’envoi</th>
                            <th>Détails</th>
                        </tr>
                        </thead>
                        <tbody>
                        {notificationPage.items.map(({ id, deliveries, first, readCount: groupReadCount }) => (
                            <Fragment key={id}>
                                <tr>
                                    <td data-label="Type">
                                        <span className={`badge badge--${first.type.toLowerCase()}`}>{first.type}</span>
                                    </td>
                                    <td data-label="Message">{first.message}</td>
                                    <td data-label="Destinataires">{groupReadCount}/{deliveries.length} lue(s)</td>
                                    <td data-label="Date d’envoi">{new Date(first.createdAt).toLocaleString('fr-FR')}</td>
                                    <td data-label="Détails">
                                        <button className="secondary-button" type="button" aria-expanded={expandedGroupId === id} onClick={() => setExpandedGroupId(expandedGroupId === id ? null : id)}>
                                            <ChevronDown size={15} />{expandedGroupId === id ? 'Masquer' : `Voir ${deliveries.length} élève(s)`}
                                        </button>
                                    </td>
                                </tr>
                                {expandedGroupId === id && (
                                    <tr className="notification-details-row">
                                        <td colSpan="5">
                                            <NotificationDeliveries deliveries={deliveries} />
                                        </td>
                                    </tr>
                                )}
                            </Fragment>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
            {!loading && !error && <PaginationControls {...notificationPage} />}
        </div>
    </section>
}

function NotificationDeliveries({ deliveries }) {
    const page = usePagination(deliveries)

    return <>
        <ul className="notification-delivery-list">
            {page.items.map((delivery) => (
                <li key={delivery.id}>
                    <span className="notification-recipient">
                        <strong>{[delivery.etudiantPrenom, delivery.etudiantNom].filter(Boolean).join(' ') || 'Élève'}</strong>
                        {delivery.etudiantUsername && <small className="notification-recipient-username">{delivery.etudiantUsername}</small>}
                    </span>
                    <span>{delivery.lu ? 'Vue' : 'Non vue'}</span>
                </li>
            ))}
        </ul>
        <PaginationControls {...page} />
    </>
}