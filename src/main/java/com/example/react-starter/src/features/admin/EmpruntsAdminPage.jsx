import { useEffect, useState } from 'react'
import { BookOpen, RotateCcw, CheckCircle } from 'lucide-react'
import { api } from '../../api/client.js'

export default function EmpruntsAdminPage() {
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', emprunts: [] })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const [statut, setStatut] = useState('')
    const requestKey = String(retryVersion) + statut
    const emprunts = result.key === requestKey ? result.emprunts : []
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    useEffect(() => {
        const controller = new AbortController()
        const params = statut ? { statut } : {}
        api.get('/api/emprunts', { params, signal: controller.signal })
            .then(({ data }) => setResult({ key: requestKey, emprunts: data }))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setFailure({ key: requestKey, message: err.response?.data?.message || 'Impossible de charger les emprunts.' })
            })
        return () => controller.abort()
    }, [requestKey, statut])

    async function handleRetour(id) {
        try {
            await api.patch(`/api/emprunts/${id}/retour`)
            setRetryVersion(v => v + 1)
        } catch (err) {
            alert(err.response?.data?.message || 'Impossible de valider le retour.')
        }
    }

    return <section className="content admin-page emprunts-admin-page">
        <header className="admin-heading">
            <span className="eyebrow">ADMINISTRATION DE L'ETABLISSEMENT</span>
            <h1>Gestion des emprunts</h1>
            <p>Consultez les emprunts en cours et validez les retours.</p>
        </header>

        <div className="filters-bar">
            <label htmlFor="filtre-statut">Filtrer par statut</label>
            <select
                id="filtre-statut"
                value={statut}
                onChange={e => { setStatut(e.target.value); setRetryVersion(v => v + 1) }}
            >
                <option value="">Tous</option>
                <option value="ACTIF">Actif</option>
                <option value="RETARDE">En retard</option>
                <option value="EXPIRE">Expire</option>
            </select>
        </div>

        {loading && (
            <div className="catalog-state" role="status">
                Chargement des emprunts...
            </div>
        )}

        {!loading && error && (
            <div className="catalog-state catalog-state-error" role="alert">
                <p>{error}</p>
                <button className="secondary-button" onClick={() => setRetryVersion(v => v + 1)}>
                    <RotateCcw size={15} />Reessayer
                </button>
            </div>
        )}

        {!loading && !error && emprunts.length === 0 && (
            <div className="catalog-state">
                <BookOpen size={24} />
                <h2>Aucun emprunt</h2>
                <p>Aucun emprunt ne correspond aux criteres selectionnes.</p>
            </div>
        )}

        {!loading && !error && emprunts.length > 0 && (
            <div className="emprunts-table">
                <table>
                    <thead>
                    <tr>
                        <th>Ouvrage</th>
                        <th>Etudiant</th>
                        <th>Date emprunt</th>
                        <th>Date expiration</th>
                        <th>Statut</th>
                        <th>Action</th>
                    </tr>
                    </thead>
                    <tbody>
                    {emprunts.map(emprunt => (
                        <tr key={emprunt.id}>
                            <td data-label="Ouvrage">{emprunt.titreOuvrage}</td>
                            <td data-label="Étudiant">{emprunt.etudiantId || '—'}</td>
                            <td data-label="Date emprunt">{new Date(emprunt.dateEmprunt).toLocaleDateString('fr-FR')}</td>
                            <td data-label="Date expiration">{new Date(emprunt.dateExpiration).toLocaleDateString('fr-FR')}</td>
                            <td data-label="Statut">
                  <span className={`badge badge--${emprunt.statut.toLowerCase()}`}>
                    {emprunt.statut}
                  </span>
                            </td>
                            <td data-label="Action">
                                {emprunt.statut === 'ACTIF' || emprunt.statut === 'RETARDE' ? (
                                    <button
                                        className="secondary-button"
                                        onClick={() => handleRetour(emprunt.id)}
                                    >
                                        <CheckCircle size={15} />Valider retour
                                    </button>
                                ) : (
                                    <span>—</span>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        )}
    </section>
}