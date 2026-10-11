import { useEffect, useRef, useState } from 'react'
import { FileUp, LibraryBig, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'

export default function BookPdfManagementPage() {
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', books: [] })
  const [failure, setFailure] = useState({ key: '', message: '' })
  const requestKey = String(retryVersion)
  const books = result.key === requestKey ? result.books : []
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    const controller = new AbortController()
    api.get('/api/ouvrages', { signal: controller.signal })
      .then(({ data }) => setResult({ key: requestKey, books: data }))
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: requestError.response?.data?.message || 'Impossible de charger les ouvrages.' })
      })
    return () => controller.abort()
  }, [requestKey])

  return <section className="content book-files-page">
    <div className="book-files-heading"><span className="eyebrow">ADMINISTRATION DE L’ÉTABLISSEMENT</span><h1>Documents des ouvrages</h1><p>Téléversez le PDF qui sera disponible à la lecture après un emprunt actif.</p></div>
    <div className="book-files-security"><FileUp size={18} /><span>PDF uniquement, 20 Mo maximum. La lecture reste réservée à l’étudiant ayant emprunté l’ouvrage.</span></div>
    {loading && <div className="catalog-state" role="status">Chargement des ouvrages…</div>}
    {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && books.length === 0 && <div className="catalog-state"><LibraryBig size={24} /><h2>Aucun ouvrage actif</h2><p>Les ouvrages de votre établissement apparaîtront ici.</p></div>}
    {!loading && !error && books.length > 0 && <div className="book-files-list">{books.map((book) => <BookPdfRow key={book.id} book={book} />)}</div>}
  </section>
}

function BookPdfRow({ book }) {
  const fileInput = useRef(null)
  const [file, setFile] = useState(null)
  const [uploading, setUploading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    if (!file) return
    setError('')
    setMessage('')
    if (file.size > 20 * 1024 * 1024) {
      setError('Le PDF ne peut pas dépasser 20 Mo.')
      return
    }
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      setError('Sélectionnez un fichier avec l’extension .pdf.')
      return
    }

    setUploading(true)
    const formData = new FormData()
    formData.append('file', file)
    try {
      await api.post(`/api/ouvrages/${book.id}/fichier`, formData)
      setMessage('PDF téléversé. Les étudiants pourront le lire après emprunt.')
      setFile(null)
      if (fileInput.current) fileInput.current.value = ''
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Le téléversement a échoué.')
    } finally {
      setUploading(false)
    }
  }

  return <article className="book-file-row">
    <div className="book-file-info"><span>{book.categorie?.nom || 'Ouvrage'}</span><h2>{book.titre}</h2><p>{book.auteur}</p></div>
    <form className="book-file-form" onSubmit={handleSubmit}>
      <label className="pdf-picker">Choisir un PDF<input ref={fileInput} type="file" accept=".pdf,application/pdf" aria-label={`Choisir le PDF de ${book.titre}`} onChange={(event) => { setFile(event.target.files?.[0] || null); setMessage(''); setError('') }} disabled={uploading} /></label>
      {file && <span className="selected-pdf">{file.name}</span>}
      <button className="primary-button" type="submit" disabled={!file || uploading}>{uploading ? 'Téléversement…' : file ? 'Téléverser le PDF' : 'Sélectionner un PDF'}<FileUp size={16} /></button>
      {message && <p className="upload-message" role="status">{message}</p>}
      {error && <p className="upload-error" role="alert">{error}</p>}
    </form>
  </article>
}