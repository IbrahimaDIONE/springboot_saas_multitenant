import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Clock, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'
import './history.css'

const tabs = [
    { value: '', label: 'Tout' },
    { value: 'EMPRUNT', label: 'Emprunts' },
    { value: 'CONSULTATION', label: 'Consultations' },
]
const resourceLinks = { OUVRAGE: '/app/etudiant/catalogue', MEMOIRE: '/app/etudiant/memoires' }
const resourceLabels = { OUVRAGE: 'un ouvrage', MEMOIRE: 'un mémoire' }

function formatDate(value) {
    return value ? new Date(value).toLocaleString('fr-FR', { dateStyle: 'medium', timeStyle: 'short' }) : '—'
}

export default function HistoryPage() {
    const [type, setType] = useState('')
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', items: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const requestKey = `${type}:${retryVersion}`
    const items = result.key === requestKey ? result.items : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/historique', { params: { type: type || undefined }, signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, items: data }))
            .catch((requestError) => {
                if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: requestError.response?.data?.message || 'Impossible de charger votre historique.' })
            })
        return () => controller.abort()
    }, [type, retryVersion, requestKey])

    return <section className="content loans-page">
        <div className="loans-heading"><span className="eyebrow">ESPACE ÉTUDIANT</span><h1>Mon historique</h1><p>Retrouvez vos emprunts et vos consultations récentes.</p></div>
        <div className="history-tabs">{tabs.map((tab) => <button key={tab.value} className={`history-tab${type === tab.value ? ' active' : ''}`} onClick={() => setType(tab.value)}>{tab.label}</button>)}</div>
        {loading && <div className="catalog-state" role="status">Chargement de l’historique…</div>}
        {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
        {!loading && !error && items.length === 0 && <div className="catalog-state"><Clock size={24} /><h2>Rien à afficher</h2><p>Votre activité apparaîtra ici au fil de vos emprunts et consultations.</p></div>}
        {!loading && !error && items.length > 0 && <div className="loans-list">{items.map((item, index) => <article className="loan-item" key={`${item.typeRessource}-${item.ressourceId}-${index}`}>
            <div className="loan-book-icon"><Clock size={18} /></div>
            <div className="loan-book-info"><h2>{item.titre || `Consultation de ${resourceLabels[item.typeRessource] || 'une ressource'}`}</h2></div>
            <span className={`history-action${item.typeAction === 'EMPRUNT' ? ' history-action--emprunt' : ''}`}>{item.typeAction === 'EMPRUNT' ? 'Emprunt' : 'Consultation'}</span>
            <div className="loan-dates"><span>{formatDate(item.date)}</span></div>
            <Link className="details-link" to={`${resourceLinks[item.typeRessource] || '/app/etudiant'}/${item.ressourceId}`}>Voir<ArrowRight size={15} /></Link>
        </article>)}</div>}
    </section>
}