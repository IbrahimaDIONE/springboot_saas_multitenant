import { useEffect, useState } from 'react'
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import { ArrowLeft, ArrowRight, BookOpenCheck, CircleHelp, LogOut, UserRound } from 'lucide-react'
import { useAuth } from './auth/useAuth.js'
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
      <nav className="main-nav" aria-label="Navigation principale"><div className="nav-item active"><BookOpenCheck size={18} />Accueil</div></nav>
      <div className="sidebar-bottom">
        <a className="nav-item help-link" href="mailto:bibliotheque@universite.edu"><CircleHelp size={18} />Aide</a>
        <button className="nav-item logout-link" onClick={handleLogout}><LogOut size={18} />Se déconnecter</button>
        <div className="tenant-card"><span className="tenant-mark"><UserRound size={16} /></span><span><strong>{user.username}</strong><small>{roleLabels[user.role]}</small></span></div>
      </div>
    </aside>
    <main className="main-area">
      <header className="topbar">
        <div className="breadcrumb"><span>BiblioUniv</span><span>/</span><strong>Accueil</strong></div>
        <div className="topbar-actions"><span className="role-pill">{roleLabels[user.role]}</span>{user.tenantId && <span className="tenant-label">{user.tenantId}</span>}</div>
      </header>
      {children}
    </main>
  </div>
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