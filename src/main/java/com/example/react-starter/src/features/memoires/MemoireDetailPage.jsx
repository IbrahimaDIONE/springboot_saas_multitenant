import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Download, Heart, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'
import MemoireVisual from './MemoireVisual.jsx'
import './memoires.css'

export default function MemoireDetailPage() {
    const { memoireId } = useParams()
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', memoire: null })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const [favori, setFavori] = useState(null)
    const [favoriBusy, setFavoriBusy] = useState(false)
    const [downloadingId, setDownloadingId] = useState('')
    const requestKey = `${memoireId}:${retryVersion}`
    const memoire = result.key === requestKey ? result.memoire : null
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    useEffect(() => {
        const controller = new AbortController()
        api.get(`/api/memoires/${memoireId}`, { signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, memoire: data }))
            .catch((requestError) => {
                if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: requestError.response?.data?.message || 'Impossible de charger ce mémoire.' })
            })
        return () => controller.abort()
    }, [memoireId, retryVersion, requestKey])

    useEffect(() => {
        let active = true
        api.get('/api/favoris')
            .then(({ data }) => {
                if (!active) return
                setFavori(data.find((item) => item.typeRessource === 'MEMOIRE' && item.ressourceId === memoireId) || null)
            })
            .catch(() => {})
        return () => { active = false }
    }, [memoireId])

    async function toggleFavori() {
        setFavoriBusy(true)
        try {
            if (favori) {
                await api.delete(`/api/favoris/${favori.id}`)
                setFavori(null)
            } else {
                const { data } = await api.post('/api/favoris', { typeRessource: 'MEMOIRE', ressourceId: memoireId })
                setFavori(data)
            }
        } catch { /* bouton repasse disponible, l'utilisateur peut réessayer */ }
        setFavoriBusy(false)
    }

    async function downloadFile(fichier) {
        setDownloadingId(fichier.id)
        try {
            const { data } = await api.get(`/api/memoires/${memoireId}/fichiers/${fichier.id}`, { responseType: 'blob' })
            const objectUrl = URL.createObjectURL(data)
            const link = document.createElement('a')
            link.href = objectUrl
            link.download = fichier.nomOriginal
            link.click()
            URL.revokeObjectURL(objectUrl)
        } catch { /* l'utilisateur peut réessayer */ }
        setDownloadingId('')
    }

    return <section className="content book-detail-page">
        <Link className="back-link" to="/app/etudiant/memoires"><ArrowLeft size={16} />Retour aux mémoires</Link>
        {loading && <div className="catalog-state" role="status">Chargement de la fiche…</div>}
        {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
        {!loading && !error && memoire && <div className="book-detail-layout">
            <MemoireVisual memoire={memoire} large />
            <div className="book-detail-info">
                <div className="memoire-detail-favorite">
                    <span className="eyebrow">{memoire.annee}</span>
                    <button className={`save-button${favori ? ' saved' : ''}`} onClick={toggleFavori} disabled={favoriBusy} aria-pressed={Boolean(favori)} aria-label={favori ? 'Retirer des favoris' : 'Ajouter aux favoris'}><Heart size={15} /></button>
                </div>
                <h1>{memoire.titre}</h1>
                <p className="book-detail-author">{memoire.auteur}</p>
                <div className="book-detail-metadata">
                    {memoire.filiere?.nom && <div><span>FILIÈRE</span><strong>{memoire.filiere.nom}</strong></div>}
                    {memoire.niveau?.nom && <div><span>NIVEAU</span><strong>{memoire.niveau.nom}</strong></div>}
                    <div><span>ENCADREUR</span><strong>{memoire.encadreur}</strong></div>
                </div>
                <div className="book-detail-summary"><h2>Résumé</h2><p>{memoire.resume || 'Aucun résumé n’est disponible pour ce mémoire.'}</p></div>
                <div className="memoire-files">
                    <h2>Fichiers</h2>
                    {(!memoire.fichiers || memoire.fichiers.length === 0) && <p>Aucun fichier n’a encore été déposé pour ce mémoire.</p>}
                    {memoire.fichiers?.map((fichier) => <div className="memoire-file-row" key={fichier.id}>
                        <div className="memoire-file-info"><strong>{fichier.nomOriginal}</strong><span>{Math.round(fichier.tailleOctets / 1024)} Ko</span></div>
                        {fichier.disponible
                            ? <button className="secondary-button" onClick={() => downloadFile(fichier)} disabled={downloadingId === fichier.id}>{downloadingId === fichier.id ? 'Téléchargement…' : 'Télécharger'}<Download size={15} /></button>
                            : <span className="memoire-file-unavailable">Indisponible</span>}
                    </div>)}
                </div>
            </div>
        </div>}
    </section>
}