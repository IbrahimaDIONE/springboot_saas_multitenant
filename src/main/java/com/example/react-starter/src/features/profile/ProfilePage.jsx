import { useEffect, useState } from 'react'
import { Save, UserRound } from 'lucide-react'
import { api } from '../../api/client.js'

const roleLabels = {
  ETUDIANT: 'Étudiant',
  ADMIN_ETABLISSEMENT: 'Administrateur établissement',
  ADMIN_PLATEFORME: 'Administrateur plateforme',
}

function getErrorMessage(error) {
  return error.response?.data?.message || 'Impossible de charger le profil. Réessayez.'
}

export default function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let active = true
    api.get('/api/profil')
      .then(({ data }) => {
        if (!active) return
        setProfile(data)
        setForm({ nom: data.nom || '', prenom: data.prenom || '', email: data.email || '' })
      })
      .catch((requestError) => {
        if (active) setError(getErrorMessage(requestError))
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => { active = false }
  }, [])

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
    setNotice('')
    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setNotice('')
    try {
      const { data } = await api.put('/api/profil', {
        nom: form.nom.trim(),
        prenom: form.prenom.trim(),
        email: form.email.trim(),
      })
      setProfile(data)
      setForm({ nom: data.nom || '', prenom: data.prenom || '', email: data.email || '' })
      setNotice('Votre profil a été mis à jour.')
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSaving(false)
    }
  }

  return <section className="content profile-page">
    <div className="profile-heading"><span className="eyebrow">ESPACE ÉTUDIANT</span><h1>Mon profil</h1><p>Consultez et mettez à jour vos informations personnelles.</p></div>
    {loading && <div className="profile-state" role="status">Chargement de votre profil…</div>}
    {!loading && error && !form && <div className="profile-state profile-error" role="alert">{error}<button className="text-button" onClick={() => window.location.reload()}>Réessayer</button></div>}
    {form && <form className="profile-form" onSubmit={handleSubmit}>
      <div className="profile-account"><span className="profile-avatar"><UserRound size={20} /></span><div><strong>{[profile.prenom, profile.nom].filter(Boolean).join(' ') || profile.username}</strong><span>{roleLabels[profile.role] || profile.role}</span></div><span className="account-readonly">Compte universitaire</span></div>
      <div className="profile-fields">
        <label htmlFor="profile-prenom">Prénom<input id="profile-prenom" name="prenom" value={form.prenom} onChange={updateField} maxLength={100} autoComplete="given-name" required disabled={saving} /></label>
        <label htmlFor="profile-nom">Nom<input id="profile-nom" name="nom" value={form.nom} onChange={updateField} maxLength={100} autoComplete="family-name" required disabled={saving} /></label>
        <label className="profile-email" htmlFor="profile-email">Adresse e-mail<input id="profile-email" name="email" type="email" value={form.email} onChange={updateField} maxLength={150} autoComplete="email" required disabled={saving} /></label>
      </div>
      {(error || notice) && <p className={error ? 'profile-message profile-message-error' : 'profile-message'} role={error ? 'alert' : 'status'}>{error || notice}</p>}
      <div className="profile-actions"><span>Nom et prénom : 100 caractères maximum. Email : 150 maximum.</span><button className="primary-button" type="submit" disabled={saving}>{saving ? 'Enregistrement…' : 'Enregistrer les modifications'}<Save size={16} /></button></div>
    </form>}
  </section>
}