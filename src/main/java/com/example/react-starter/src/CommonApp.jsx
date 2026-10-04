import { useEffect, useState } from 'react'
import { Navigate, NavLink, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { ArrowLeft, ArrowRight, Bell, BookOpen, BookOpenCheck, Building2, ChartNoAxesColumnIncreasing, CircleHelp, ClipboardList, FileText, LogOut, ShieldAlert, Tags, UserRound, Users } from 'lucide-react'
import { useAuth } from './auth/useAuth.js'
import BookPdfManagementPage from './features/admin/BookPdfManagementPage.jsx'
import BookManagementPage from './features/admin/BookManagementPage.jsx'
import MemoireManagementPage from './features/admin/MemoireManagementPage.jsx'
import ReferenceManagementPage from './features/admin/ReferenceManagementPage.jsx'
import StudentManagementPage from './features/admin/StudentManagementPage.jsx'
import BookCatalogPage from './features/catalog/BookCatalogPage.jsx'
import BookDetailPage from './features/catalog/BookDetailPage.jsx'
import MyLoansPage from './features/loans/MyLoansPage.jsx'
import OnlineReadingPage from './features/loans/OnlineReadingPage.jsx'
import ProfilePage from './features/profile/ProfilePage.jsx'
import './App.css'

const roleLabels = {
  ETUDIANT: 'Étudiant',
  ADMIN_ETABLISSEMENT: 'Administrateur établissement',
  ADMIN_PLATEFORME: 'Administrateur plateforme',
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
    <Route path="/app/etablissement/dashboard" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><Lot4Placeholder title="Tableau de bord" /></RoleRoute>} />
    <Route path="/app/etablissement/emprunts" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><Lot4Placeholder title="Gestion des emprunts" /></RoleRoute>} />
    <Route path="/app/etablissement/notifications" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><Lot4Placeholder title="Notifications" /></RoleRoute>} />
    <Route path="/app/etablissement/penalites" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><Lot4Placeholder title="Pénalités" /></RoleRoute>} />
    <Route path="/app/plateforme/etablissements" element={<RoleRoute role="ADMIN_PLATEFORME"><Lot4Placeholder title="Établissements" /></RoleRoute>} />
    <Route path="/app/etablissement" element={<RoleRoute role="ADMIN_ETABLISSEMENT"><RoleHome /></RoleRoute>} />
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
  return <section className="content common-home">
    <span className="eyebrow">VOTRE ESPACE SÉCURISÉ</span>
    <h1>Bienvenue, {user.username}.</h1>
    <p>Vous êtes connecté en tant que <strong>{roleLabels[user.role]}</strong>.</p>
    <div className="common-welcome"><span className="welcome-icon"><BookOpenCheck size={22} /></span><div><h2>Votre espace BiblioUniv</h2><p>La base commune est prête. Les fonctionnalités de votre espace seront ajoutées progressivement.</p></div></div>
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
      <p className="login-help">Besoin d’aide ? <a href="mailto:bibliotheque@universite.edu">Contacter la bibliothèque</a></p>
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
      </nav>
      <div className="sidebar-bottom">
        <a className="nav-item help-link" href="mailto:bibliotheque@universite.edu"><CircleHelp size={18} />Aide</a>
        <button className="nav-item logout-link" onClick={handleLogout}><LogOut size={18} />Se déconnecter</button>
        <div className="tenant-card"><span className="tenant-mark"><UserRound size={16} /></span><span><strong>{user.username}</strong><small>{roleLabels[user.role]}</small></span></div>
      </div>
    </aside>
    <main className="main-area">
      <header className="topbar">
        <div className="breadcrumb"><span>BiblioUniv</span><span>/</span><strong>{getBreadcrumbLabel(location.pathname)}</strong></div>
        <div className="topbar-actions"><span className="role-pill">{roleLabels[user.role]}</span>{user.tenantId && <span className="tenant-label">{user.tenantId}</span>}</div>
      </header>
      {children}
    </main>
  </div>
}

function Lot4Placeholder({ title }) {
  const { user } = useAuth()
  return <section className="content common-home">
    <span className="eyebrow">LOT 4 · {roleLabels[user.role].toUpperCase()}</span>
    <h1>{title}</h1>
    <p>Le routage est prêt. Cet écran attend le composant du lot 4.</p>
  </section>
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