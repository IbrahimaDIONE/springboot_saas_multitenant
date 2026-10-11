import { useEffect, useState } from 'react'
import { BookOpenCheck, Users, BookOpen, AlertTriangle, RotateCcw } from 'lucide-react'
import { api } from '../../api/client.js'

export default function DashboardPage() {
    const [retryVersion, setRetryVersion] = useState(0)
    const [result, setResult] = useState({ key: '', data: null })
    const [failure, setFailure] = useState({ key: '', message: '' })
    const requestKey = String(retryVersion)
    const data = result.key === requestKey ? result.data : null
    const loading = result.key !== requestKey && failure.key !== requestKey
    const error = failure.key === requestKey ? failure.message : ''

    useEffect(() => {
        const controller = new AbortController()
        api.get('/api/dashboard/etablissement', { signal: controller.signal })
            .then(({ data: d }) => setResult({ key: requestKey, data: d }))
            .catch((err) => {
                if (err.code !== 'ERR_CANCELED')
                    setFailure({ key: requestKey, message: err.response?.data?.message || 'Impossible de charger le tableau de bord.' })
            })
        return () => controller.abort()
    }, [requestKey])

    return <section className="content admin-page dashboard-page">
        <header className="admin-heading">
            <span className="eyebrow">ADMINISTRATION DE L'ETABLISSEMENT</span>
            <h1>Tableau de bord</h1>
            <p>Vue d'ensemble de l'activite de votre etablissement.</p>
        </header>

        {loading && (
            <div className="catalog-state" role="status">
                Chargement du tableau de bord...
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

        {!loading && !error && data && (
            <div className="dashboard-grid">
                <StatCard
                    icon={<BookOpen size={22} />}
                    label="Emprunts en cours"
                    value={data.empruntsEnCours ?? 0}
                    color="corail"
                />
                <StatCard
                    icon={<Users size={22} />}
                    label="Etudiants inscrits"
                    value={data.nombreEtudiants ?? 0}
                    color="encre"
                />
                <StatCard
                    icon={<BookOpenCheck size={22} />}
                    label="Ressources disponibles"
                    value={data.nombreRessources ?? 0}
                    color="safran"
                />
                <StatCard
                    icon={<AlertTriangle size={22} />}
                    label="Emprunts en retard"
                    value={data.empruntsRetardes ?? 0}
                    color="alerte"
                />
            </div>
        )}
    </section>
}

function StatCard({ icon, label, value, color }) {
    return (
        <div className={`stat-card stat-card--${color}`}>
            <div className="stat-icon">{icon}</div>
            <div className="stat-body">
                <span className="stat-value">{value}</span>
                <span className="stat-label">{label}</span>
            </div>
        </div>
    )
}