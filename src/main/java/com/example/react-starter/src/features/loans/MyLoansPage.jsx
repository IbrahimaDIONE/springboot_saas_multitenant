import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, BookOpen, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'

const statusLabels = {
  ACTIF: 'En cours',
  EXPIRE: 'Expiré',
  RETARDE: 'En retard',
  RETOURNE: 'Retourné',
}

function formatDate(value) {
  return value ? new Date(value).toLocaleDateString('fr-FR') : '—'
}

export default function MyLoansPage() {
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', loans: [] })
  const [failure, setFailure] = useState({ key: '', message: '' })
  const requestKey = String(retryVersion)
  const loans = result.key === requestKey ? result.loans : []
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    const controller = new AbortController()
    api.get('/api/emprunts/mes-emprunts', { signal: controller.signal })
      .then(({ data }) => setResult({ key: requestKey, loans: data }))
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: requestError.response?.data?.message || 'Impossible de charger vos emprunts.' })
      })
    return () => controller.abort()
  }, [requestKey])

  return <section className="content loans-page">
    <div className="loans-heading"><span className="eyebrow">ESPACE ÉTUDIANT</span><h1>Mes emprunts</h1><p>Suivez les ouvrages empruntés et leurs échéances.</p></div>
    {loading && <div className="catalog-state" role="status">Chargement de vos emprunts…</div>}
    {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && loans.length === 0 && <div className="catalog-state"><BookOpen size={24} /><h2>Aucun emprunt</h2><p>Les ouvrages que vous empruntez apparaîtront ici.</p><Link className="details-link" to="/app/etudiant/catalogue">Parcourir le catalogue<ArrowRight size={15} /></Link></div>}
    {!loading && !error && loans.length > 0 && <div className="loans-list">{loans.map((loan) => <article className="loan-item" key={loan.id}>
      <div className="loan-book-icon"><BookOpen size={20} /></div>
      <div className="loan-book-info"><h2>{loan.titreOuvrage}</h2><p>{loan.auteurOuvrage}</p></div>
      <span className={`loan-status loan-status--${loan.statut.toLowerCase()}`}>{statusLabels[loan.statut] || loan.statut}</span>
      <div className="loan-dates"><span>Emprunté le <strong>{formatDate(loan.dateEmprunt)}</strong></span><span>Échéance <strong>{formatDate(loan.dateExpiration)}</strong></span></div>
      {loan.statut === 'ACTIF' ? <Link className="primary-button loan-read-link" to={`/app/etudiant/emprunts/${loan.id}/lire`}>Lire en ligne<ArrowRight size={15} /></Link> : <span className="loan-read-unavailable">Lecture indisponible</span>}
    </article>)}</div>}
  </section>
}