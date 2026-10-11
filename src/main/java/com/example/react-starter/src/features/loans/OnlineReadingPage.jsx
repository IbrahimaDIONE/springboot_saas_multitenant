import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, ExternalLink, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'

export default function OnlineReadingPage() {
  const { empruntId } = useParams()
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', reading: null, documentUrl: '' })
  const [failure, setFailure] = useState({ key: '', message: '' })
  const requestKey = `${empruntId}:${retryVersion}`
  const reading = result.key === requestKey ? result.reading : null
  const documentUrl = result.key === requestKey ? result.documentUrl : ''
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    const controller = new AbortController()
    let objectUrl = ''
    async function loadDocument() {
      try {
        const { data: reading } = await api.get(`/api/emprunts/${empruntId}/lire`, { signal: controller.signal })
        if (!reading.urlFichier) {
          setResult({ key: requestKey, reading, documentUrl: '' })
          return
        }
        const expectedPath = `/api/emprunts/${empruntId}/lire/fichier`
        if (reading.urlFichier !== expectedPath) throw new Error('URL de lecture invalide.')
        const { data: file } = await api.get(expectedPath, { responseType: 'blob', signal: controller.signal })
        if (controller.signal.aborted) return
        objectUrl = URL.createObjectURL(file)
        setResult({ key: requestKey, reading, documentUrl: objectUrl })
      } catch (requestError) {
        if (requestError.code !== 'ERR_CANCELED' && !controller.signal.aborted) {
          setFailure({ key: requestKey, message: requestError.response?.data?.message || requestError.message || 'Impossible d’ouvrir cet emprunt.' })
        }
      }
    }
    loadDocument()
    return () => {
      controller.abort()
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [empruntId, retryVersion, requestKey])

  return <section className="content reading-page">
    <Link className="back-link" to="/app/etudiant/emprunts"><ArrowLeft size={16} />Retour à mes emprunts</Link>
    {loading && <div className="catalog-state" role="status">Ouverture de la lecture…</div>}
    {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && reading && <>
      <div className="reading-heading"><span className="eyebrow">LECTURE EN LIGNE</span><h1>{reading.titreOuvrage}</h1></div>
      {documentUrl ? <div className="reading-document"><div className="reading-toolbar"><span>Document de l’emprunt</span><a href={documentUrl} target="_blank" rel="noreferrer">Ouvrir dans un nouvel onglet<ExternalLink size={15} /></a></div><iframe title={`Lecture : ${reading.titreOuvrage}`} src={documentUrl} referrerPolicy="no-referrer" /></div> : <div className="catalog-state reading-unavailable"><BookOpenNotice /><h2>Document indisponible</h2><p>L’administrateur de votre établissement n’a pas encore téléversé le PDF. Contactez la bibliothèque si besoin.</p></div>}
    </>}
  </section>
}

function BookOpenNotice() {
  return <span className="reading-empty-mark" aria-hidden="true">PDF</span>
}