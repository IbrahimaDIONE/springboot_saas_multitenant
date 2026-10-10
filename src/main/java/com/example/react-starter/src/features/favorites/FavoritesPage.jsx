import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Heart, RotateCcw, Trash2 } from 'lucide-react'
import { api } from '../../api/client.js'
import './favorites.css'

const typeLabels = { OUVRAGE: 'Ouvrage', MEMOIRE: 'Mémoire' }
const typeLinks = { OUVRAGE: '/app/etudiant/catalogue', MEMOIRE: '/app/etudiant/memoires' }

export default function FavoritesPage() {
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', favoris: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const [removingId, setRemovingId] = useState('')
    const requestKey = String(retryVersion)
    const favoris = result.key === requestKey ? result.favoris : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/favoris', { signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, favoris: data }))
            .catch((requestError) => {
                if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: requestError.response?.data?.message || 'Impossible de charger vos favoris.' })
            })
        return () => controller.abort()
    }, [requestKey])

    async function removeFavori(id) {
        setRemovingId(id)
        try {
            await api.delete(`/api/favoris/${id}`)
            setResult((previous) => ({ key: previous.key, favoris: previous.favoris.filter((item) => item.id !== id) }))
        } catch { /* l'utilisateur peut réessayer */ }
        setRemovingId('')
    }

    return <section className="content loans-page">
        <div className="loans-heading"><span className="eyebrow">ESPACE ÉTUDIANT</span><h1>Mes favoris</h1><p>Retrouvez les ouvrages et mémoires que vous avez mis de côté.</p></div>
        {loading && <div className="catalog-state" role="status">Chargement de vos favoris…</div>}
        {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
        {!loading && !error && favoris.length === 0 && <div className="catalog-state"><Heart size={24} /><h2>Aucun favori</h2><p>Ajoutez un ouvrage ou un mémoire à vos favoris depuis le catalogue, les mémoires ou la fiche détail.</p></div>}
        {!loading && !error && favoris.length > 0 && <div className="loans-list">{favoris.map((favori) => <article className="loan-item" key={favori.id}>
            <div className="loan-book-icon"><Heart size={18} /></div>
            <div className="loan-book-info"><h2>{favori.titre}</h2></div>
            <span className="favorite-type">{typeLabels[favori.typeRessource] || favori.typeRessource}</span>
            <Link className="details-link" to={`${typeLinks[favori.typeRessource] || '/app/etudiant'}/${favori.ressourceId}`}>Voir la fiche<ArrowRight size={15} /></Link>
            <button className="secondary-button favorite-remove" onClick={() => removeFavori(favori.id)} disabled={removingId === favori.id}><Trash2 size={14} />{removingId === favori.id ? 'Retrait…' : 'Retirer'}</button>
        </article>)}</div>}
    </section>
}