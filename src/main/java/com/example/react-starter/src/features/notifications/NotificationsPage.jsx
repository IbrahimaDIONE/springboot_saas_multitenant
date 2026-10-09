import { useEffect, useState } from 'react'
import { Bell, CheckCheck, RotateCcw, ShieldAlert } from 'lucide-react'
import { api } from '../../api/client.js'
import './notifications.css'

const typeLabels = {
    NOUVELLE_RESSOURCE: 'Nouvelle ressource',
    RAPPEL_ECHEANCE: 'Rappel d’échéance',
    AVERTISSEMENT_RETARD: 'Avertissement de retard',
    PENALITE: 'Pénalité',
}
const consequenceLabels = {
    AVERTISSEMENT: 'Avertissement',
    SUSPENSION_TEMPORAIRE: 'Suspension temporaire',
}

function formatDate(value) {
    return value ? new Date(value).toLocaleString('fr-FR', { dateStyle: 'medium', timeStyle: 'short' }) : '—'
}

export default function NotificationsPage() {
    const [tab, setTab] = useState('notifications')
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', notifications: [], penalites: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const [markingId, setMarkingId] = useState('')
    const requestKey = String(retryVersion)
    const notifications = result.key === requestKey ? result.notifications : []
    const penalites = result.key === requestKey ? result.penalites : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''
    const unreadCount = notifications.filter((notification) => !notification.lu).length

    useEffect(() => {
        const controller = new AbortController()
        Promise.all([
            api.get('/api/notifications/mes-notifications', { signal: controller.signal }),
            api.get('/api/penalites/mes-penalites', { signal: controller.signal }),
        ])
            .then(([notificationResponse, penaliteResponse]) => setResult({ key: requestKey, notifications: notificationResponse.data, penalites: penaliteResponse.data }))
            .catch((requestError) => {
                if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: requestError.response?.data?.message || 'Impossible de charger vos notifications.' })
            })
        return () => controller.abort()
    }, [requestKey])

    async function markAsRead(id) {
        setMarkingId(id)
        try {
            const { data } = await api.patch(`/api/notifications/${id}/lire`)
            setResult((previous) => ({ ...previous, notifications: previous.notifications.map((notification) => notification.id === id ? data : notification) }))
        } catch { /* l'utilisateur peut réessayer */ }
        setMarkingId('')
    }

    return <section className="content loans-page">
        <div className="loans-heading"><span className="eyebrow">ESPACE ÉTUDIANT</span><h1>Notifications</h1><p>Suivez les messages de votre bibliothèque et vos éventuelles pénalités.</p></div>
        <div className="notif-tabs" role="tablist">
            <button role="tab" aria-selected={tab === 'notifications'} className={`notif-tab${tab === 'notifications' ? ' active' : ''}`} onClick={() => setTab('notifications')}>Notifications{unreadCount > 0 && <span className="notif-count">{unreadCount} non lue{unreadCount > 1 ? 's' : ''}</span>}</button>
            <button role="tab" aria-selected={tab === 'penalites'} className={`notif-tab${tab === 'penalites' ? ' active' : ''}`} onClick={() => setTab('penalites')}>Pénalités{penalites.length > 0 && <span className="notif-count">{penalites.length}</span>}</button>
        </div>
        {loading && <div className="catalog-state" role="status">Chargement…</div>}
        {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}

        {!loading && !error && tab === 'notifications' && (notifications.length === 0
            ? <div className="catalog-state"><Bell size={24} /><h2>Aucune notification</h2><p>Vos notifications apparaîtront ici.</p></div>
            : <div className="notif-list">{notifications.map((notification) => <article className={`notif-item${notification.lu ? '' : ' unread'}`} key={notification.id}>
                <div className="notif-icon"><Bell size={18} /></div>
                <div className="notif-body">
                    <p>{notification.message}</p>
                    <div className="notif-meta"><span className="notif-type">{typeLabels[notification.type] || notification.type}</span><span>{formatDate(notification.createdAt)}</span>{!notification.lu && <span className="notif-unread-label">Non lue</span>}</div>
                </div>
                {!notification.lu && <button className="secondary-button" onClick={() => markAsRead(notification.id)} disabled={markingId === notification.id}><CheckCheck size={14} />{markingId === notification.id ? 'Mise à jour…' : 'Marquer comme lue'}</button>}
            </article>)}</div>)}

        {!loading && !error && tab === 'penalites' && (penalites.length === 0
            ? <div className="catalog-state"><ShieldAlert size={24} /><h2>Aucune pénalité</h2><p>Vous n’avez aucune pénalité en cours.</p></div>
            : <div className="notif-list">{penalites.map((penalite) => <article className="penalty-item" key={penalite.id}>
                <div className="notif-icon"><ShieldAlert size={18} /></div>
                <div className="notif-body"><p>{penalite.motif}</p><div className="notif-meta"><span>Appliquée le {formatDate(penalite.dateApplication)}</span></div></div>
                <span className="penalty-consequence">{consequenceLabels[penalite.consequence] || penalite.consequence}</span>
            </article>)}</div>)}
    </section>
}