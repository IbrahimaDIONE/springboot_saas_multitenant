import { useEffect, useMemo, useState } from 'react'
import { Archive, BookOpen, FileUp, Pencil, Plus, RotateCcw, Search, Trash2, X } from 'lucide-react'
import { Link } from 'react-router-dom'
import { api } from '../../api/client.js'
import './admin.css'

const emptyBook = { titre: '', auteur: '', resume: '', filiereId: '', niveauId: '', categorieId: '' }

function errorMessage(error, fallback) {
  return error.response?.data?.message || fallback
}

export default function BookManagementPage() {
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', books: [], filieres: [], niveaux: [], categories: [] })
  const [failure, setFailure] = useState({ key: '', message: '' })
  const [search, setSearch] = useState('')
  const [showArchives, setShowArchives] = useState(false)
  const [formOpen, setFormOpen] = useState(false)
  const [editingBook, setEditingBook] = useState(null)
  const [form, setForm] = useState(emptyBook)
  const [saving, setSaving] = useState(false)
  const [notice, setNotice] = useState('')
  const [actionError, setActionError] = useState('')
  const requestKey = JSON.stringify([showArchives, retryVersion])
  const currentResult = result.key === requestKey ? result : { books: [], filieres: [], niveaux: [], categories: [] }
  const { books, filieres, niveaux, categories } = currentResult
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      api.get(showArchives ? '/api/ouvrages/archives' : '/api/ouvrages', { signal: controller.signal }),
      api.get('/api/filieres', { signal: controller.signal }),
      api.get('/api/niveaux', { signal: controller.signal }),
      api.get('/api/categories', { signal: controller.signal }),
    ])
      .then(([bookResponse, filiereResponse, niveauResponse, categorieResponse]) => {
        setResult({
          key: requestKey,
          books: bookResponse.data,
          filieres: filiereResponse.data,
          niveaux: niveauResponse.data,
          categories: categorieResponse.data,
        })
      })
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') {
          setFailure({ key: requestKey, message: errorMessage(requestError, 'Impossible de charger les ouvrages et leurs référentiels.') })
        }
      })
    return () => controller.abort()
  }, [requestKey, showArchives])

  const visibleBooks = useMemo(() => {
    const term = search.trim().toLocaleLowerCase()
    if (!term) return books
    return books.filter((book) => [book.titre, book.auteur, book.filiere?.nom, book.categorie?.nom]
      .some((value) => value?.toLocaleLowerCase().includes(term)))
  }, [books, search])

  function startCreate() {
    setFormOpen(true)
    setEditingBook(null)
    setForm(emptyBook)
    setActionError('')
    setNotice('')
  }

  function startEdit(book) {
    setFormOpen(true)
    setEditingBook(book)
    setForm({
      titre: book.titre,
      auteur: book.auteur,
      resume: book.resume,
      filiereId: book.filiere?.id || '',
      niveauId: book.niveau?.id || '',
      categorieId: book.categorie?.id || '',
    })
    setActionError('')
    setNotice('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setActionError('')
    setNotice('')
    const payload = {
      ...form,
      filiereId: form.filiereId,
      niveauId: form.niveauId,
      categorieId: form.categorieId || null,
    }
    try {
      if (editingBook) {
        await api.put(`/api/ouvrages/${editingBook.id}`, payload)
        setNotice('Les informations de l’ouvrage ont été mises à jour.')
      } else {
        await api.post('/api/ouvrages', payload)
        setNotice('L’ouvrage a été ajouté au catalogue.')
      }
      setForm(emptyBook)
      setEditingBook(null)
      setFormOpen(false)
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible d’enregistrer cet ouvrage.'))
    } finally {
      setSaving(false)
    }
  }

  async function changeStatus(book, action) {
    setActionError('')
    setNotice('')
    try {
      await api.patch(`/api/ouvrages/${book.id}/${action}`)
      setNotice(action === 'archiver' ? 'L’ouvrage a été archivé.' : 'L’ouvrage a été réactivé.')
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de modifier le statut de cet ouvrage.'))
    }
  }

  async function deleteBook(book) {
    if (!window.confirm(`Supprimer définitivement « ${book.titre} » ?`)) return
    setActionError('')
    setNotice('')
    try {
      await api.delete(`/api/ouvrages/${book.id}`)
      setNotice('L’ouvrage a été supprimé.')
      if (editingBook?.id === book.id) startCreate()
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de supprimer cet ouvrage.'))
    }
  }

  return <section className="content admin-page">
    <header className="admin-heading">
      <span className="eyebrow">ADMINISTRATION DE L’ÉTABLISSEMENT</span>
      <h1>Gestion des ouvrages</h1>
      <p>Constituez et entretenez le catalogue de votre bibliothèque.</p>
    </header>

    <div className="admin-toolbar">
      <div className="admin-tabs" aria-label="Filtrer les ouvrages">
        <button type="button" className={!showArchives ? 'selected' : ''} onClick={() => { setShowArchives(false); setSearch(''); setFormOpen(false); setEditingBook(null); setActionError('') }}>Ouvrages actifs</button>
        <button type="button" className={showArchives ? 'selected' : ''} onClick={() => { setShowArchives(true); setSearch(''); setFormOpen(false); setEditingBook(null); setActionError('') }}>Archives</button>
      </div>
      <label className="admin-search">
        <Search size={16} aria-hidden="true" />
        <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Rechercher un titre ou un auteur" aria-label="Rechercher un ouvrage" />
      </label>
      {!showArchives && <button className="primary-button" type="button" onClick={startCreate}><Plus size={16} />Ajouter un ouvrage</button>}
    </div>

    {!showArchives && formOpen && <form className="admin-form" onSubmit={handleSubmit}>
      <div className="admin-form-heading">
        <div><span className="eyebrow">{editingBook ? 'MODIFICATION' : 'NOUVELLE RESSOURCE'}</span><h2>{editingBook ? 'Modifier un ouvrage' : 'Ajouter un ouvrage'}</h2></div>
        <button className="icon-button" type="button" aria-label={editingBook ? 'Annuler la modification' : 'Fermer le formulaire'} onClick={() => { setFormOpen(false); setEditingBook(null); setForm(emptyBook); setActionError('') }}><X size={16} /></button>
      </div>
      <div className="admin-fields">
        <label>Titre<input name="titre" value={form.titre} onChange={(event) => setForm({ ...form, titre: event.target.value })} maxLength={200} required /></label>
        <label>Auteur<input name="auteur" value={form.auteur} onChange={(event) => setForm({ ...form, auteur: event.target.value })} maxLength={150} required /></label>
        <label>Filière<select value={form.filiereId} onChange={(event) => setForm({ ...form, filiereId: event.target.value })} required><option value="">Choisir une filière</option>{filieres.map((item) => <option key={item.id} value={item.id}>{item.nom}</option>)}</select></label>
        <label>Niveau<select value={form.niveauId} onChange={(event) => setForm({ ...form, niveauId: event.target.value })} required><option value="">Choisir un niveau</option>{niveaux.map((item) => <option key={item.id} value={item.id}>{item.nom}</option>)}</select></label>
        <label>Catégorie<select value={form.categorieId} onChange={(event) => setForm({ ...form, categorieId: event.target.value })}><option value="">Sans catégorie</option>{categories.map((item) => <option key={item.id} value={item.id}>{item.nom}</option>)}</select></label>
        <label className="admin-field-wide">Résumé<textarea value={form.resume} onChange={(event) => setForm({ ...form, resume: event.target.value })} rows={3} required /></label>
      </div>
      <div className="admin-form-actions">
        <button className="primary-button" type="submit" disabled={saving}>{saving ? 'Enregistrement…' : editingBook ? 'Enregistrer les modifications' : 'Ajouter au catalogue'}</button>
        <Link className="admin-secondary-link" to="/app/etablissement/documents"><FileUp size={15} />Gérer les PDF des ouvrages</Link>
      </div>
    </form>}

    {notice && <p className="admin-notice" role="status">{notice}</p>}
    {actionError && <p className="admin-error" role="alert">{actionError}</p>}
    {loading && <div className="admin-state" role="status">Chargement des ouvrages…</div>}
    {!loading && error && <div className="admin-state admin-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && visibleBooks.length === 0 && <div className="admin-state"><BookOpen size={23} /><h2>{showArchives ? 'Aucun ouvrage archivé' : 'Aucun ouvrage trouvé'}</h2><p>{search ? 'Essayez un autre terme de recherche.' : 'Les ouvrages de votre établissement apparaîtront ici.'}</p></div>}
    {!loading && !error && visibleBooks.length > 0 && <div className="admin-record-list">
      {visibleBooks.map((book) => <article className="admin-record" key={book.id}>
        <div className="admin-record-main">
          <span className="admin-record-eyebrow">{book.categorie?.nom || 'Ouvrage'}{book.filiere?.nom ? ` · ${book.filiere.nom}` : ''}</span>
          <h2>{book.titre}</h2>
          <p>{book.auteur} <span>·</span> {book.niveau?.nom || 'Niveau non défini'}</p>
          {book.resume && <p className="admin-record-summary">{book.resume}</p>}
        </div>
        <div className="admin-record-actions">
          {showArchives
            ? <button className="secondary-button" onClick={() => changeStatus(book, 'reactiver')}><RotateCcw size={15} />Réactiver</button>
            : <><button className="secondary-button" onClick={() => startEdit(book)}><Pencil size={15} />Modifier</button><button className="secondary-button" onClick={() => changeStatus(book, 'archiver')}><Archive size={15} />Archiver</button></>}
          <button className="danger-button" onClick={() => deleteBook(book)}><Trash2 size={15} />Supprimer</button>
        </div>
      </article>)}
    </div>}
  </section>
}
