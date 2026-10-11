import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, ArrowRight, BookOpen, Check, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'
import BookVisual from './BookVisual.jsx'

export default function BookDetailPage() {
  const { ouvrageId } = useParams()
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', book: null })
  const [failure, setFailure] = useState({ key: '', message: '' })
  const [borrowing, setBorrowing] = useState(false)
  const [borrowed, setBorrowed] = useState(null)
  const [borrowError, setBorrowError] = useState('')
  const requestKey = `${ouvrageId}:${retryVersion}`
  const book = result.key === requestKey ? result.book : null
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    const controller = new AbortController()
    api.get(`/api/ouvrages/${ouvrageId}`, { signal: controller.signal })
      .then(({ data }) => setResult({ key: requestKey, book: data }))
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: requestError.response?.data?.message || 'Impossible de charger cet ouvrage.' })
      })
    return () => controller.abort()
  }, [ouvrageId, retryVersion, requestKey])

  async function borrowBook() {
    setBorrowing(true)
    setBorrowError('')
    try {
      const { data } = await api.post('/api/emprunts', { ouvrageId: book.id })
      setBorrowed(data)
    } catch (requestError) {
      setBorrowError(requestError.response?.data?.message || 'La demande d’emprunt n’a pas abouti.')
    } finally {
      setBorrowing(false)
    }
  }

  return <section className="content book-detail-page">
    <Link className="back-link" to="/app/etudiant/catalogue"><ArrowLeft size={16} />Retour au catalogue</Link>
    {loading && <div className="catalog-state" role="status">Chargement de la fiche…</div>}
    {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && book && <div className="book-detail-layout">
      <BookVisual book={book} large />
      <div className="book-detail-info"><span className="eyebrow">{book.categorie?.nom || 'OUVRAGE'}</span><h1>{book.titre}</h1><p className="book-detail-author">{book.auteur}</p>
        <div className="book-detail-metadata">{book.filiere?.nom && <div><span>FILIÈRE</span><strong>{book.filiere.nom}</strong></div>}{book.niveau?.nom && <div><span>NIVEAU</span><strong>{book.niveau.nom}</strong></div>}</div>
        <div className="book-detail-summary"><h2>Résumé</h2><p>{book.resume || 'Aucun résumé n’est disponible pour cet ouvrage.'}</p></div>
        {borrowed ? <div className="borrow-success" role="status"><Check size={17} /><span>Emprunt enregistré. Retour prévu le {new Date(borrowed.dateExpiration).toLocaleDateString('fr-FR')}.</span><Link to="/app/etudiant/emprunts">Voir mes emprunts<ArrowRight size={15} /></Link></div> : <div className="borrow-actions"><button className="primary-button" onClick={borrowBook} disabled={borrowing}>{borrowing ? 'Enregistrement…' : 'Emprunter cet ouvrage'}<BookOpen size={16} /></button>{borrowError && <p role="alert">{borrowError}</p>}</div>}
      </div>
    </div>}
  </section>
}