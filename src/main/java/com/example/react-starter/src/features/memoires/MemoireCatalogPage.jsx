import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, FileText, Heart, RotateCcw, Search } from 'lucide-react'
import { api } from '../../api/client.js'
import MemoireVisual from './MemoireVisual.jsx'

function getErrorMessage(error) {
    return error.response?.data?.message || 'Impossible de charger les mémoires. Réessayez.'
}

export default function MemoireCatalogPage() {
    const [filieres, setFilieres] = useState([])
    const [niveaux, setNiveaux] = useState([])
    const [search, setSearch] = useState('')
    const [filiereId, setFiliereId] = useState('')
    const [niveauId, setNiveauId] = useState('')
    const [annee, setAnnee] = useState('')
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', memoires: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const [optionsError, setOptionsError] = useState('')
    const [favorites, setFavorites] = useState([])
    const [busyId, setBusyId] = useState('')
    const requestKey = JSON.stringify([search, filiereId, niveauId, annee, retryVersion])
    const memoires = result.key === requestKey ? result.memoires : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    useEffect(() => {
        let active = true
        Promise.all([api.get('/api/filieres'), api.get('/api/niveaux')])
            .then(([filiereResponse, niveauResponse]) => {
                if (!active) return
                setFilieres(filiereResponse.data)
                setNiveaux(niveauResponse.data)
            })
            .catch(() => { if (active) setOptionsError('Les filtres filière et niveau n’ont pas pu être chargés.') })
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
        api.get('/api/memoires', {
            params: {
                search: search.trim() || undefined,
                filiereId: filiereId || undefined,
                niveauId: niveauId || undefined,
                annee: annee || undefined,
            },
            signal: controller.signal,
        })
            .then(({ data }) => setResult({ key: requestKey, memoires: data }))
            .catch((requestError) => {
                if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: getErrorMessage(requestError) })
            })
        return () => controller.abort()
    }, [search, filiereId, niveauId, annee, retryVersion, requestKey])

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
        setAnnee('')
    }

    return <section className="content catalog-page">
        <div className="catalog-heading"><span className="eyebrow">ESPACE ÉTUDIANT</span><h1>Les mémoires</h1><p>Consultez les mémoires soutenus au sein de votre établissement.</p></div>
        <div className="catalog-toolbar">
            <label className="catalog-search"><Search size={18} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Rechercher par titre, auteur ou résumé" aria-label="Rechercher un mémoire" /></label>
            <label className="catalog-filter">Filière<select value={filiereId} onChange={(event) => setFiliereId(event.target.value)} disabled={Boolean(optionsError)}><option value="">Toutes les filières</option>{filieres.map((filiere) => <option key={filiere.id} value={filiere.id}>{filiere.nom}</option>)}</select></label>
            <label className="catalog-filter">Niveau<select value={niveauId} onChange={(event) => setNiveauId(event.target.value)} disabled={Boolean(optionsError)}><option value="">Tous les niveaux</option>{niveaux.map((niveau) => <option key={niveau.id} value={niveau.id}>{niveau.nom}</option>)}</select></label>
            <label className="catalog-filter">Année<input type="number" value={annee} onChange={(event) => setAnnee(event.target.value)} placeholder="Ex. 2025" /></label>
        </div>
        {optionsError && <p className="catalog-inline-error" role="alert">{optionsError}</p>}
        <div className="catalog-results-heading"><h2>Mémoires</h2>{!loading && !error && <span>{memoires.length} résultat{memoires.length > 1 ? 's' : ''}</span>}</div>
        {loading && <div className="catalog-state" role="status">Chargement des mémoires…</div>}
        {!loading && error && <div className="catalog-state catalog-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
        {!loading && !error && memoires.length === 0 && <div className="catalog-state"><FileText size={24} /><h2>Aucun mémoire trouvé</h2><p>Essaie un autre terme ou modifie les filtres.</p><button className="text-button" onClick={resetFilters}>Effacer les filtres</button></div>}
        {!loading && !error && memoires.length > 0 && <div className="catalog-book-grid">{memoires.map((memoire) => {
            const saved = isFavorite('MEMOIRE', memoire.id)
            return <article className="catalog-book-card" key={memoire.id}>
                <MemoireVisual memoire={memoire} />
                <div className="catalog-book-info"><div className="catalog-book-meta"><span>{memoire.encadreur}</span>{memoire.filiere?.nom && <span>{memoire.filiere.nom}</span>}</div><h3>{memoire.titre}</h3><p>{memoire.auteur}</p>
                    {memoire.niveau?.nom && <span className="catalog-level">{memoire.niveau.nom}</span>}
                    <div className="book-card-actions">
                        <Link className="details-link" to={`/app/etudiant/memoires/${memoire.id}`}>Consulter la fiche<ArrowRight size={15} /></Link>
                        <button type="button" className={`save-button${saved ? ' saved' : ''}`} onClick={() => toggleFavori('MEMOIRE', memoire.id)} disabled={busyId === memoire.id} aria-pressed={saved} aria-label={saved ? 'Retirer des favoris' : 'Ajouter aux favoris'}>
                            <Heart size={15} fill={saved ? 'currentColor' : 'none'} />
                        </button>
                    </div>
                </div>
            </article>
        })}</div>}
    </section>
}