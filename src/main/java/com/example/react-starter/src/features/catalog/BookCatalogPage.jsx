import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, BookOpen, Heart, RotateCcw, Search } from 'lucide-react'
import { api } from '../../api/client.js'
import BookVisual from './BookVisual.jsx'

function getErrorMessage(error) {
  return error.response?.data?.message || 'Impossible de charger le catalogue. Réessayez.'
}

export default function BookCatalogPage() {
  const [filieres, setFilieres] = useState([])
  const [niveaux, setNiveaux] = useState([])
  const [categories, setCategories] = useState([])
  const [search, setSearch] = useState('')
  const [filiereId, setFiliereId] = useState('')
  const [niveauId, setNiveauId] = useState('')
  const [categorieId, setCategorieId] = useState('')
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', books: [] })
  const [failure, setFailure] = useState({ key: '', message: '' })
  const [optionsError, setOptionsError] = useState('')
  const [favorites, setFavorites] = useState([])
  const [busyId, setBusyId] = useState('')
  const requestKey = JSON.stringify([search, filiereId, niveauId, categorieId, retryVersion])
  const books = result.key === requestKey ? result.books : []
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    let active = true
    Promise.all([api.get('/api/filieres'), api.get('/api/niveaux'), api.get('/api/categories')])
      .then(([filiereResponse, niveauResponse, categorieResponse]) => {
        if (!active) return
        setFilieres(filiereResponse.data)
        setNiveaux(niveauResponse.data)
        setCategories(categorieResponse.data)
      })
      .catch(() => {
        if (active) setOptionsError('Les filtres filière, niveau et catégorie n’ont pas pu être chargés.')
      })
    return () => { active = false }
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    api.get('/api/favoris', { signal: controller.signal })
      .then(({ data }) => setFavorites(Array.isArray(data) ? data : []))
      .catch(() => setFavorites([]))
    return () => controller.abort()
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    api.get('/api/ouvrages', {
      params: {
        search: search.trim() || undefined,
        filiereId: filiereId || undefined,
        niveauId: niveauId || undefined,
        categorieId: categorieId || undefined,
      },
      signal: controller.signal,
    })
      .then(({ data }) => setResult({ key: requestKey, books: data }))
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: getErrorMessage(requestError) })
      })
    return () => controller.abort()
  }, [search, filiereId, niveauId, categorieId, retryVersion, requestKey])

  function isFavorite(type, id) {
    return favorites.some((favori) => favori.typeRessource === type && favori.ressourceId === id)
  }

  async function toggleFavori(type, id) {
    const existingFavorite = favorites.find((favori) => favori.typeRessource === type && favori.ressourceId === id)
    setBusyId(id)
    try {
      if (existingFavorite) {
        await api.delete(`/api/favoris/${existingFavorite.id}`)
        setFavorites((previous) => previous.filter((favori) => favori.id !== existingFavorite.id))
      } else {
        const { data } = await api.post('/api/favoris', { typeRessource: type, ressourceId: id })
        setFavorites((previous) => [data, ...previous])
      }
    } catch {
      // L'utilisateur peut réessayer.
    } finally {
      setBusyId('')
    }
  }

  function resetFilters() {
    setSearch('')
    setFiliereId('')
    setNiveauId('')
    setCategorieId('')
  }

  return <section className="content catalog-page">
    <div className="catalog-heading"><span className="eyebrow">ESPACE ÉTUDIANT</span><h1>Le catalogue</h1><p>Explorez les ouvrages proposés par votre établissement.</p></div>
    <div className="catalog-toolbar">
      <label className="catalog-search"><Search size={18} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Rechercher par titre, auteur ou résumé" aria-label="Rechercher un ouvrage" /></label>
      <label className="catalog-filter">Filière<select value={filiereId} onChange={(event) => setFiliereId(event.target.value)} disabled={Boolean(optionsError)}><option value="">Toutes les filières</option>{filieres.map((filiere) => <option key={filiere.id} value={filiere.id}>{filiere.nom}</option>)}</select></label>
      <label className="catalog-filter">Niveau<select value={niveauId} onChange={(event) => setNiveauId(event.target.value)} disabled={Boolean(optionsError)}><option value="">Tous les niveaux</option>{niveaux.map((niveau) => <option key={niveau.id} value={niveau.id}>{niveau.nom}</option>)}</select></label>
      <label className="catalog-filter">Catégorie<select value={categorieId} onChange={(event) => setCategorieId(event.target.value)} disabled={Boolean(optionsError)}><option value="">Toutes les catégories</option>{categories.map((categorie) => <option key={categorie.id} value={categorie.id}>{categorie.nom}</option>)}</select></label>
    </div>
    {optionsError && <p className="catalog-inline-error" role="alert">{optionsError}</p>}
    <div className="catalog-results-heading"><h2>Ouvrages</h2>{!loading && !error && <span>{books.length} résultat{books.length > 1 ? 's' : ''}</span>}</div>
    {loading && <div className="catalog-state" role="status">Chargement des ouvrages…</div>}
    {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && books.length === 0 && <div className="catalog-state"><BookOpen size={24} /><h2>Aucun ouvrage trouvé</h2><p>Essaie un autre terme ou modifie les filtres.</p><button className="text-button" onClick={resetFilters}>Effacer les filtres</button></div>}
    {!loading && !error && books.length > 0 && <div className="catalog-book-grid">{books.map((book) => {
      const saved = isFavorite('OUVRAGE', book.id)
      return <article className="catalog-book-card" key={book.id}>
        <BookVisual book={book} />
        <div className="catalog-book-info"><div className="catalog-book-meta"><span>{book.categorie?.nom || 'Sans catégorie'}</span>{book.filiere?.nom && <span>{book.filiere.nom}</span>}</div><h3>{book.titre}</h3><p>{book.auteur}</p>
          {book.niveau?.nom && <span className="catalog-level">{book.niveau.nom}</span>}
          <div className="book-card-actions">
            <Link className="details-link" to={`/app/etudiant/catalogue/${book.id}`}>Consulter la fiche<ArrowRight size={15} /></Link>
            <button type="button" className={`save-button${saved ? ' saved' : ''}`} onClick={() => toggleFavori('OUVRAGE', book.id)} disabled={busyId === book.id} aria-pressed={saved} aria-label={saved ? 'Retirer des favoris' : 'Ajouter aux favoris'}>
              <Heart size={15} fill={saved ? 'currentColor' : 'none'} />
            </button>
          </div>
        </div>
      </article>
    })}</div>}
  </section>
}