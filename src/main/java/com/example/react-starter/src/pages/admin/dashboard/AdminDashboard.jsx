function AdminDashboard() {
    const stats = [
        {
            title: 'Ouvrages',
            value: '—',
            icon: 'bi-book',
            color: 'primary',
            link: '/admin/ouvrages',
        },
        {
            title: 'Mémoires',
            value: '—',
            icon: 'bi-journal-text',
            color: 'success',
            link: '/admin/memoires',
        },
        {
            title: 'Étudiants',
            value: '—',
            icon: 'bi-people',
            color: 'warning',
            link: '/admin/etudiants',
        },
        {
            title: 'Catégories',
            value: '—',
            icon: 'bi-tags',
            color: 'info',
            link: '/admin/categories',
        },
    ]

    return (
        <div>
            <div className="mb-4">
                <h1 className="fw-bold mb-1">Tableau de bord</h1>
                <p className="text-muted mb-0">
                    Administration de votre établissement
                </p>
            </div>

            <div className="row g-4 mb-4">
                {stats.map((stat) => (
                    <div className="col-xl-3 col-md-6" key={stat.title}>
                        <a
                            href={stat.link}
                            className="text-decoration-none"
                        >
                            <div className="card border-0 shadow-sm h-100">
                                <div className="card-body p-4">
                                    <div className="d-flex justify-content-between align-items-start">
                                        <div>
                                            <p className="text-muted small mb-2">
                                                {stat.title}
                                            </p>

                                            <h2 className="fw-bold text-dark mb-0">
                                                {stat.value}
                                            </h2>
                                        </div>

                                        <div
                                            className={`bg-${stat.color} bg-opacity-10 text-${stat.color} rounded-3 p-3`}
                                        >
                                            <i className={`bi ${stat.icon} fs-4`}></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                ))}
            </div>

            <div className="card border-0 shadow-sm">
                <div className="card-body p-4">
                    <h5 className="fw-bold mb-2">
                        Bienvenue dans BiblioUniv
                    </h5>

                    <p className="text-muted mb-0">
                        Utilisez le menu de navigation pour gérer les
                        ressources et les étudiants de votre établissement.
                    </p>
                </div>
            </div>
        </div>
    )
}

export default AdminDashboard