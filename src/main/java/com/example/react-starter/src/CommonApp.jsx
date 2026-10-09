import { useEffect, useState } from 'react'
import { Navigate, NavLink, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { ArrowLeft, ArrowRight, Bell, BookOpen, BookOpenCheck, Building2, ChartNoAxesColumnIncreasing, ClipboardList, FileText, LogOut, ShieldAlert, Tags, UserRound, Users } from 'lucide-react'
import { useAuth } from './auth/useAuth.js'
import BookPdfManagementPage from './features/admin/BookPdfManagementPage.jsx'
import BookManagementPage from './features/admin/BookManagementPage.jsx'
import MemoireManagementPage from './features/admin/MemoireManagementPage.jsx'
import ReferenceManagementPage from './features/admin/ReferenceManagementPage.jsx'
import StudentManagementPage from './features/admin/StudentManagementPage.jsx'
import DashboardPage from './features/admin/DashboardPage.jsx'
import EmpruntsAdminPage from './features/admin/EmpruntsAdminPage.jsx'
import NotificationsAdminPage from './features/admin/NotificationsAdminPage.jsx'
import ReglesPenaliteAdminPage from './features/admin/ReglesPenaliteAdminPage.jsx'
import EtablissementsPage from './features/admin/EtablissementsPage.jsx'
import RolesAdminPage from './features/admin/RolesAdminPage.jsx'
import BookCatalogPage from './features/catalog/BookCatalogPage.jsx'
import BookDetailPage from './features/catalog/BookDetailPage.jsx'
import MyLoansPage from './features/loans/MyLoansPage.jsx'
import OnlineReadingPage from './features/loans/OnlineReadingPage.jsx'
import ProfilePage from './features/profile/ProfilePage.jsx'
import { api } from './api/client.js'
import './App.css'
import './features/admin/admin.css'

const roleLabels = {
  ETUDIANT: 'Étudiant',
  ADMIN_ETABLISSEMENT: 'Administrateur établissement',
  ADMIN_PLATEFORME: 'Administrateur plateforme',
}

const homeContent = {
  ETUDIANT: {
    eyebrow: 'ESPACE ÉTUDIANT',
    description: 'Retrouvez vos ressources et vos emprunts depuis votre espace personnel.',
    actions: [
      { label: 'Explorer le catalogue', detail: 'Rechercher un ouvrage dans la bibliothèque.', path: '/app/etudiant/catalogue', icon: BookOpen },
      { label: 'Mes emprunts', detail: 'Consulter les échéances et reprendre une lecture.', path: '/app/etudiant/emprunts', icon: ClipboardList },
      { label: 'Mon profil', detail: 'Vérifier vos informations de compte.', path: '/app/etudiant/profil', icon: UserRound },
    ],
  },
  ADMIN_ETABLISSEMENT: {
    eyebrow: 'ADMINISTRATION DE L’ÉTABLISSEMENT',
    description: 'Suivez l’activité et accédez aux outils de gestion de votre établissement.',
    statsEndpoint: '/api/dashboard/etablissement',
    stats: [
      { label: 'Étudiants inscrits', key: 'nombreEtudiants', color: 'ink', icon: Users },
      { label: 'Ressources disponibles', key: 'nombreRessources', color: 'gold', icon: BookOpen },
      { label: 'Emprunts en cours', key: 'empruntsEnCours', color: 'coral', icon: ClipboardList },
      { label: 'Emprunts en retard', key: 'empruntsRetardes', color: 'alert', icon: ShieldAlert },
    ],
    actions: [
      { label: 'Tableau de bord', detail: 'Consulter les indicateurs de la bibliothèque.', path: '/app/etablissement/dashboard', icon: ChartNoAxesColumnIncreasing },
      { label: 'Ouvrages', detail: 'Gérer le catalogue de votre établissement.', path: '/app/etablissement/ouvrages', icon: BookOpen },
      { label: 'Mémoires', detail: 'Gérer les mémoires et leurs fichiers.', path: '/app/etablissement/memoires', icon: FileText },
      { label: 'Étudiants', detail: 'Gérer les comptes et les inscriptions.', path: '/app/etablissement/etudiants', icon: Users },
      { label: 'Référentiels', detail: 'Gérer les filières, niveaux et catégories.', path: '/app/etablissement/referentiels', icon: Tags },
      { label: 'Emprunts', detail: 'Suivre les emprunts et valider les retours.', path: '/app/etablissement/emprunts', icon: ClipboardList },
      { label: 'Notifications', detail: 'Consulter et envoyer des notifications.', path: '/app/etablissement/notifications', icon: Bell },
      { label: 'Pénalités', detail: 'Gérer les règles et les pénalités appliquées.', path: '/app/etablissement/penalites', icon: ShieldAlert },
    ],
  },
  ADMIN_PLATEFORME: {
    eyebrow: 'ADMINISTRATION DE LA PLATEFORME',
    description: 'Pilotez les établissements hébergés et les rôles de la plateforme.',
    statsEndpoint: '/api/etablissements/statistiques',
    stats: [
      { label: 'Établissements', key: 'etablissements', color: 'ink', icon: Building2 },
      { label: 'Établissements actifs', key: 'etablissementsActifs', color: 'gold', icon: ChartNoAxesColumnIncreasing },
      { label: 'Utilisateurs', key: 'utilisateurs', color: 'coral', icon: Users },
      { label: 'Ressources', key: 'ressources', color: 'blue', icon: BookOpen },
    ],
    actions: [
      { label: 'Établissements', detail: 'Créer, configurer et suivre les établissements.', path: '/app/plateforme/etablissements', icon: Building2 },
      { label: 'Rôles', detail: 'Gérer les rôles disponibles sur la plateforme.', path: '/app/plateforme/roles', icon: Users },
    ],
  },
}

function CommonApp() {
  return <><AppRoutes /><ApiFeedback /></>
}

function AppRoutes() {
  const { user, loading, homeByRole } = useAuth()
  if (loading) return <LoadingScreen />

  return <Routes>
    <Route path="/" element={<Navigate to={user ? homeByRole[user.role] : '/login'} replace />} />
    <Route path="/login" element={user ? <Navigate to={homeByRole[user.role]} replace /> : <LoginPage />} />
    <Route path="/403" element={<ForbiddenPage />} />
    <Route path="/app/etudiant" element={<RoleRoute role="ETUDIANT"><RoleHome /></RoleRoute>} />
    <Route path="/app/etudiant/profil" element={<RoleRoute role="ETUDIANT"><ProfilePage /></RoleRoute>} />
    <Route path="/app/etudiant/catalogue" element={<RoleRoute role="ETUDIANT"><BookCatalogPage /></RoleRoute>} />
    <Route path="/app/etudiant/catalogue/:ouvrageId" element={<RoleRoute role="ETUDIANT"><BookDetailPage /></RoleRoute>} />
    <Route path="/app/etudiant/emprunts" element={<RoleRoute role="ETUDIANT"><MyLoansPage /></RoleRoute>} />
    <Route path="/app/etudiant/emprunts/:empruntId/lire" element={<RoleRoute role="ETUDIANT"><OnlineReadingPage /></RoleRoute>} />
    <Route path="/app/etablissement/documents" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><BookPdfManagementPage /></RoleRoute>} />
    <Route path="/app/etablissement/ouvrages" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><BookManagementPage /></RoleRoute>} />
    <Route path="/app/etablissement/memoires" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><MemoireManagementPage /></RoleRoute>} />
    <Route path="/app/etablissement/etudiants" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><StudentManagementPage /></RoleRoute>} />
    <Route path="/app/etablissement/referentiels" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><ReferenceManagementPage /></RoleRoute>} />
    <Route path="/app/etablissement/dashboard" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><DashboardPage /></RoleRoute>} />
    <Route path="/app/etablissement/emprunts" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><EmpruntsAdminPage /></RoleRoute>} />
    <Route path="/app/etablissement/notifications" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><NotificationsAdminPage /></RoleRoute>} />
    <Route path="/app/etablissement/penalites" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><ReglesPenaliteAdminPage /></RoleRoute>} />
    <Route path="/app/plateforme/etablissements" element={<RoleRoute role="ADMIN_PLATEFORME"><EtablissementsPage /></RoleRoute>} />
    <Route path="/app/etablissement" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><RoleHome /></RoleRoute>} />
    <Route path="/app/plateforme/roles" element={<RoleRoute role="ADMIN_PLATEFORME"><RolesAdminPage /></RoleRoute>} />
    <Route path="/app/plateforme" element={<RoleRoute role="ADMIN_PLATEFORME"><RoleHome /></RoleRoute>} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
}

function RoleRoute({ role, children }) {
  const { user } = useAuth()
  const location = useLocation()
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />
  if (user.role !== role) return <Navigate to="/403" replace />
  return <AppLayout>{children}</AppLayout>
}

function RoleHome() {
  const { user } = useAuth()
  const [statsResult, setStatsResult] = useState({ role: '', data: null })
  const [statsFailure, setStatsFailure] = useState({ role: '', message: '' })
  const content = homeContent[user.role]
  const statsData = statsResult.role === user.role ? statsResult.data : null
  const statsError = statsFailure.role === user.role ? statsFailure.message : ''
  const statsLoading = Boolean(content.statsEndpoint) && !statsData && !statsError

  useEffect(() => {
    if (!content.statsEndpoint) return undefined
    const controller = new AbortController()
    api.get(content.statsEndpoint, { signal: controller.signal })
      .then(({ data }) => setStatsResult({ role: user.role, data }))
      .catch((error) => {
        if (error.code !== 'ERR_CANCELED') {
          setStatsFailure({
            role: user.role,
            message: error.response?.data?.message || 'Impossible de charger les indicateurs.',
          })
        }
      })
    return () => controller.abort()
  }, [content.statsEndpoint, user.role])

  return <section className="content common-home">
    <header className="admin-heading home-heading">
      <span className="eyebrow">{content.eyebrow}</span>
      <h1>Bienvenue, {user.username}.</h1>
      <p>{content.description}</p>
    </header>

    {statsLoading && <div className="admin-state home-stats-state" role="status">Chargement des indicateurs…</div>}
    {statsError && <p className="admin-error home-stats-error" role="alert">{statsError}</p>}
    {statsData && <div className="dashboard-grid home-stat-grid">
      {content.stats.map(({ label, key, color, icon: Icon }) => (
        <div className={`stat-card stat-card--${color}`} key={key}>
          <span className="stat-icon"><Icon size={19} /></span>
          <span className="stat-body"><strong>{statsData[key] ?? 0}</strong><small>{label}</small></span>
        </div>
      ))}
    </div>}

    <section className="home-actions-section" aria-labelledby="home-actions-title">
      <div className="home-section-heading">
        <h2 id="home-actions-title">Accès rapides</h2>
        <span>{content.actions.length} espaces disponibles</span>
      </div>
      <div className="home-action-grid">
        {content.actions.map(({ label, detail, path, icon: Icon }) => (
          <NavLink className="home-action" key={path} to={path}>
            <span className="home-action-icon"><Icon size={18} /></span>
            <span className="home-action-copy"><strong>{label}</strong><small>{detail}</small></span>
            <ArrowRight className="home-action-arrow" size={16} />
          </NavLink>
        ))}
      </div>
    </section>
  </section>
}

function LoginPage() {
  const { login, homeByRole } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    const formData = new FormData(event.currentTarget)
    try {
      const user = await login(formData.get('username'), formData.get('password'))
      navigate(homeByRole[user.role], { replace: true })
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Connexion impossible. Vérifiez vos identifiants et réessayez.')
    } finally {
      setSubmitting(false)
    }
  }

  return <main className="login-page">
    <div className="login-art" aria-hidden="true">
      <div className="login-art-copy"><span className="eyebrow">LE SAVOIR EN COMMUN</span><p>Une bibliothèque ouverte sur les idées, les campus et le monde.</p><span className="art-caption">BIBLIOUNIV · BIBLIOTHÈQUE UNIVERSITAIRE</span></div>
      <div className="art-sun" /><div className="art-book art-book-one" /><div className="art-book art-book-two" />
    </div>
    <section className="login-panel">
      <Brand />
      <div className="login-heading"><span className="eyebrow">VOTRE ESPACE</span><h1>Heureux de vous revoir.</h1><p>Connectez-vous avec votre compte universitaire.</p></div>
      <form className="login-form" onSubmit={handleSubmit}>
        <label htmlFor="username">Identifiant universitaire</label>
        <input id="username" name="username" type="text" placeholder="Votre identifiant" autoComplete="username" required disabled={submitting} />
        <div className="password-label"><label htmlFor="password">Mot de passe</label></div>
        <input id="password" name="password" type="password" placeholder="Votre mot de passe" autoComplete="current-password" required disabled={submitting} />
        <button className="primary-button login-submit" type="submit" disabled={submitting}>{submitting ? 'Connexion…' : 'Se connecter'}<ArrowRight size={17} /></button>
        {error && <p className="form-notice" role="alert">{error}</p>}
      </form>
      <span className="login-footer">Accès réservé aux membres de l’établissement</span>
    </section>
  </main>
}

function AppLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  async function handleLogout() {
    try {
      await logout()
    } finally {
      navigate('/login', { replace: true })
    }
  }

  return <div className="app-shell">
    <aside className="sidebar">
      <Brand />
      <span className="nav-caption">ESPACE DE TRAVAIL</span>
      <nav className="main-nav" aria-label="Navigation principale">
        <NavLink to={homeByRolePath(user.role)} end className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><BookOpenCheck size={18} />Accueil</NavLink>
        {user.role === 'ETUDIANT' && <NavLink to="/app/etudiant/catalogue" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><BookOpenCheck size={18} />Catalogue</NavLink>}
        {user.role === 'ETUDIANT' && <NavLink to="/app/etudiant/emprunts" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><BookOpenCheck size={18} />Mes emprunts</NavLink>}
        {user.role === 'ETUDIANT' && <NavLink to="/app/etudiant/profil" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><UserRound size={18} />Mon profil</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/ouvrages" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><BookOpen size={18} />Ouvrages</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/memoires" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><FileText size={18} />Mémoires</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/etudiants" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><Users size={18} />Étudiants</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/referentiels" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><Tags size={18} />Référentiels</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/documents" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><BookOpenCheck size={18} />Documents ouvrages</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/dashboard" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><ChartNoAxesColumnIncreasing size={18} />Tableau de bord</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/emprunts" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><ClipboardList size={18} />Emprunts</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/notifications" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><Bell size={18} />Notifications</NavLink>}
        {user.role === 'ADMIN_ETABLISSEMENT' && <NavLink to="/app/etablissement/penalites" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><ShieldAlert size={18} />Pénalités</NavLink>}
        {user.role === 'ADMIN_PLATEFORME' && <NavLink to="/app/plateforme/etablissements" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><Building2 size={18} />Établissements</NavLink>}
        {user.role === 'ADMIN_PLATEFORME' && <NavLink to="/app/plateforme/roles" className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}><Users size={18} />Rôles</NavLink>}
      </nav>
      <div className="sidebar-bottom">
        <button className="nav-item logout-link" onClick={handleLogout}><LogOut size={18} />Se déconnecter</button>
        <div className="tenant-card"><span className="tenant-mark"><UserRound size={16} /></span><span><strong>{user.username}</strong><small>{roleLabels[user.role]}</small></span></div>
      </div>
    </aside>
    <main className="main-area">
      <header className="topbar">
        <div className="breadcrumb"><span>BiblioUniv</span><span>/</span><strong>{getBreadcrumbLabel(location.pathname)}</strong></div>
        <div className="topbar-actions"><span className="role-pill">{roleLabels[user.role]}</span>{user.role !== 'ADMIN_PLATEFORME' && user.tenantId && <span className="tenant-label">{user.tenantId}</span>}</div>
      </header>
      {children}
    </main>
  </div>
}

function getBreadcrumbLabel(pathname) {
  if (pathname.endsWith('/dashboard')) return 'Tableau de bord'
  if (pathname.endsWith('/ouvrages')) return 'Gestion des ouvrages'
  if (pathname.endsWith('/memoires')) return 'Gestion des mémoires'
  if (pathname.endsWith('/etudiants')) return 'Gestion des étudiants'
  if (pathname.endsWith('/referentiels')) return 'Filières, niveaux et catégories'
  if (pathname.endsWith('/documents')) return 'Documents ouvrages'
  if (pathname === '/app/etablissement/emprunts') return 'Gestion des emprunts'
  if (pathname.startsWith('/app/etudiant/emprunts')) return pathname.endsWith('/lire') ? 'Lecture en ligne' : 'Mes emprunts'
  if (pathname.endsWith('/notifications')) return 'Notifications'
  if (pathname.endsWith('/penalites')) return 'Pénalités'
  if (pathname.endsWith('/etablissements')) return 'Établissements'
  if (pathname.endsWith('/roles')) return 'Rôles'
  return 'Accueil'
}
function homeByRolePath(role) {
  return { ETUDIANT: '/app/etudiant', ADMIN_ETABLISSEMENT: '/app/etablissement', ADMIN_PLATEFORME: '/app/plateforme' }[role]
}

function ForbiddenPage() {
  const { user, homeByRole } = useAuth()
  return <main className="status-page">
    <Brand /><span className="status-code">403</span><h1>Accès non autorisé</h1>
    <p>Cette page n’est pas disponible pour votre profil.</p>
    <button className="primary-button" onClick={() => window.location.assign(user ? homeByRole[user.role] : '/login')}><ArrowLeft size={16} />Retour à mon espace</button>
  </main>
}

function LoadingScreen() {
  return <main className="loading-screen" role="status" aria-live="polite"><span className="loading-mark"><BookOpenCheck size={21} /></span><span>Vérification de votre session…</span></main>
}

function Brand() {
  return <div className="brand"><span className="brand-icon"><BookOpenCheck size={19} /></span><span>Biblio<span>Univ</span></span></div>
}

function ApiFeedback() {
  const [forbidden, setForbidden] = useState(false)
  useEffect(() => {
    const handleForbidden = () => setForbidden(true)
    window.addEventListener('bibliouniv:forbidden', handleForbidden)
    return () => window.removeEventListener('bibliouniv:forbidden', handleForbidden)
  }, [])
  if (!forbidden) return null
  return <div className="api-feedback" role="alert">L’accès à cette ressource n’est pas autorisé.<button aria-label="Fermer" onClick={() => setForbidden(false)}>×</button></div>
}

export default CommonApp