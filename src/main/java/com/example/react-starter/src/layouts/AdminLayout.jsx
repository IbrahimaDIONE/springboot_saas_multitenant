import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import './AdminLayout.css'

function AdminLayout() {
    const navigate = useNavigate()

    const menuItems = [
        {
            section: 'PRINCIPAL',
            items: [
                { to: '/admin', icon: 'bi-speedometer2', label: 'Tableau de bord' },
            ],
        },
        {
            section: 'CATALOGUE',
            items: [
                { to: '/admin/ouvrages', icon: 'bi-book', label: 'Ouvrages' },
                { to: '/admin/memoires', icon: 'bi-journal-text', label: 'Mémoires' },
            ],
        },
        {
            section: 'ÉTUDIANTS',
            items: [
                { to: '/admin/etudiants', icon: 'bi-people', label: 'Étudiants' },
            ],
        },
        {
            section: 'PARAMÈTRES',
            items: [
                { to: '/admin/filieres', icon: 'bi-mortarboard', label: 'Filières' },
                { to: '/admin/niveaux', icon: 'bi-bar-chart-steps', label: 'Niveaux' },
                { to: '/admin/categories', icon: 'bi-tags', label: 'Catégories' },
            ],
        },
    ]

    const handleLogout = () => {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        navigate('/login')
    }

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar">
                <div className="sidebar-brand">
                    <div className="brand-icon">
                        <i className="bi bi-book-half"></i>
                    </div>

                    <div>
                        <div className="brand-name">BiblioUniv</div>
                        <small>Administration</small>
                    </div>
                </div>

                <div className="sidebar-profile">
                    <div className="profile-avatar">
                        <i className="bi bi-person-fill"></i>
                    </div>

                    <div className="profile-info">
                        <strong>Administrateur</strong>
                        <span>Établissement</span>
                    </div>
                </div>

                <nav className="sidebar-nav">
                    {menuItems.map((group) => (
                        <div className="nav-group" key={group.section}>
                            <div className="nav-section-title">
                                {group.section}
                            </div>

                            {group.items.map((item) => (
                                <NavLink
                                    key={item.to}
                                    to={item.to}
                                    end={item.to === '/admin'}
                                    className={({ isActive }) =>
                                        `admin-nav-link ${isActive ? 'active' : ''}`
                                    }
                                >
                                    <i className={`bi ${item.icon}`}></i>
                                    <span>{item.label}</span>
                                </NavLink>
                            ))}
                        </div>
                    ))}
                </nav>

                <div className="sidebar-footer">
                    <button
                        type="button"
                        className="logout-button"
                        onClick={handleLogout}
                    >
                        <i className="bi bi-box-arrow-right"></i>
                        <span>Déconnexion</span>
                    </button>
                </div>
            </aside>

            <main className="admin-main">
                <header className="admin-header">
                    <div>
            <span className="header-breadcrumb">
              BiblioUniv
            </span>
                        <i className="bi bi-chevron-right mx-2"></i>
                        <span className="header-current">
              Administration
            </span>
                    </div>

                    <div className="header-actions">
                        <button
                            type="button"
                            className="header-icon-button"
                            title="Notifications"
                        >
                            <i className="bi bi-bell"></i>
                            <span className="notification-dot"></span>
                        </button>

                        <div className="header-user">
                            <div className="header-avatar">
                                <i className="bi bi-person"></i>
                            </div>

                            <div>
                                <strong>Administrateur</strong>
                                <small>ADMIN_ETABLISSEMENT</small>
                            </div>
                        </div>
                    </div>
                </header>

                <div className="admin-content">
                    <Outlet />
                </div>
            </main>
        </div>
    )
}

export default AdminLayout