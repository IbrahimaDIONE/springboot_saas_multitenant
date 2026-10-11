import { useEffect, useMemo, useRef, useState } from 'react'
import { Check, FileSpreadsheet, Plus, RotateCcw, Search, UserRound, UserRoundX, X } from 'lucide-react'
import { api } from '../../api/client.js'
import './admin.css'

const emptyStudent = { username: '', password: '', nom: '', prenom: '', email: '', filiereId: '', niveauId: '' }

function errorMessage(error, fallback) {
  return error.response?.data?.message || fallback
}

export default function StudentManagementPage() {
  const importInput = useRef(null)
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', students: [], filieres: [], niveaux: [] })
  const [failure, setFailure] = useState({ key: '', message: '' })
  const [actionError, setActionError] = useState('')
  const [notice, setNotice] = useState('')
  const [search, setSearch] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [form, setForm] = useState(emptyStudent)
  const [saving, setSaving] = useState(false)
  const [importing, setImporting] = useState(false)
  const [importResult, setImportResult] = useState(null)
  const [changingStudentId, setChangingStudentId] = useState('')
  const requestKey = String(retryVersion)
  const currentResult = result.key === requestKey ? result : { students: [], filieres: [], niveaux: [] }
  const { students, filieres, niveaux } = currentResult
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      api.get('/api/etudiants', { signal: controller.signal }),
      api.get('/api/filieres', { signal: controller.signal }),
      api.get('/api/niveaux', { signal: controller.signal }),
    ])
      .then(([studentResponse, filiereResponse, niveauResponse]) => {
        setResult({
          key: requestKey,
          students: studentResponse.data,
          filieres: filiereResponse.data,
          niveaux: niveauResponse.data,
        })
      })
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: errorMessage(requestError, 'Impossible de charger les étudiants.') })
      })
    return () => controller.abort()
  }, [requestKey])

  const visibleStudents = useMemo(() => {
    const term = search.trim().toLocaleLowerCase()
    if (!term) return students
    return students.filter((student) => [student.nom, student.prenom, student.username, student.email]
      .some((value) => value?.toLocaleLowerCase().includes(term)))
  }, [students, search])

  async function handleCreate(event) {
    event.preventDefault()
    setSaving(true)
    setActionError('')
    setNotice('')
    try {
      await api.post('/api/etudiants', {
        ...form,
        filiereId: form.filiereId || null,
        niveauId: form.niveauId || null,
      })
      setNotice('Le compte étudiant a été créé. Il est désactivé par défaut.')
      setForm(emptyStudent)
      setFormOpen(false)
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de créer ce compte étudiant.'))
    } finally {
      setSaving(false)
    }
  }

  async function importExcel(file) {
    if (!file) return
    setActionError('')
    setNotice('')
    setImportResult(null)
    if (!file.name.toLowerCase().endsWith('.xlsx')) {
      setActionError('Le fichier d’import doit être au format .xlsx.')
      if (importInput.current) importInput.current.value = ''
      return
    }
    const formData = new FormData()
    formData.append('file', file)
    setImporting(true)
    try {
      const { data } = await api.post('/api/etudiants/import-excel', formData)
      setImportResult(data)
      setNotice(`${data.ajoutes} étudiant${data.ajoutes === 1 ? '' : 's'} ajouté${data.ajoutes === 1 ? '' : 's'} sur ${data.total} ligne${data.total === 1 ? '' : 's'}.`)
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'L’import Excel a échoué.'))
    } finally {
      setImporting(false)
      if (importInput.current) importInput.current.value = ''
    }
  }

  async function toggleStudent(student) {
    setChangingStudentId(student.id)
    setActionError('')
    setNotice('')
    try {
      const request = student.enabled
        ? api.delete(`/api/etudiants/${student.id}/activation`)
        : api.post(`/api/etudiants/${student.id}/activation`)
      const { data } = await request
      setResult((current) => ({ ...current, students: current.students.map((item) => item.id === data.id ? data : item) }))
      setNotice(student.enabled ? 'Le compte étudiant a été désactivé.' : 'Le compte étudiant a été activé.')
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de modifier le statut de cet étudiant.'))
    } finally {
      setChangingStudentId('')
    }
  }

  return <section className="content admin-page">
    <header className="admin-heading">
      <span className="eyebrow">ADMINISTRATION DE L’ÉTABLISSEMENT</span>
      <h1>Gestion des étudiants</h1>
      <p>Créez les comptes étudiants et gérez leur accès à l’espace de votre établissement.</p>
    </header>

    <div className="admin-toolbar">
      <label className="admin-search"><Search size={16} aria-hidden="true" /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Rechercher par nom, identifiant ou e-mail" aria-label="Rechercher un étudiant" /></label>
      <label className="admin-upload admin-upload-control">
        <FileSpreadsheet size={15} />{importing ? 'Importation…' : 'Importer un fichier Excel (.xlsx)'}
        <input ref={importInput} type="file" accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" disabled={importing} onChange={(event) => importExcel(event.target.files?.[0])} />
      </label>
      <button className="primary-button" type="button" onClick={() => { setFormOpen((open) => !open); setActionError(''); setNotice('') }}><Plus size={16} />Ajouter un étudiant</button>
    </div>

    <aside className="admin-import-note">
      <FileSpreadsheet size={17} />
      <p>En-têtes Excel requis : <code>username</code>, <code>password</code>, <code>nom</code>, <code>prenom</code>, <code>email</code>. Les colonnes <code>filiereId</code> et <code>niveauId</code> sont facultatives.</p>
    </aside>

    {formOpen && <form className="admin-form" onSubmit={handleCreate}>
      <div className="admin-form-heading">
        <div><span className="eyebrow">NOUVEAU COMPTE</span><h2>Ajouter un étudiant</h2></div>
        <button className="icon-button" type="button" aria-label="Fermer le formulaire" onClick={() => { setFormOpen(false); setForm(emptyStudent) }}><X size={16} /></button>
      </div>
      <div className="admin-fields">
        <label>Identifiant<input value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} maxLength={80} autoComplete="username" required /></label>
        <label>Mot de passe initial<input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} minLength={8} maxLength={100} autoComplete="new-password" required /></label>
        <label>Nom<input value={form.nom} onChange={(event) => setForm({ ...form, nom: event.target.value })} maxLength={100} required /></label>
        <label>Prénom<input value={form.prenom} onChange={(event) => setForm({ ...form, prenom: event.target.value })} maxLength={100} required /></label>
        <label>E-mail<input type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} maxLength={150} required /></label>
        <label>Filière<select value={form.filiereId} onChange={(event) => setForm({ ...form, filiereId: event.target.value })}><option value="">Non renseignée</option>{filieres.map((item) => <option key={item.id} value={item.id}>{item.nom}</option>)}</select></label>
        <label>Niveau<select value={form.niveauId} onChange={(event) => setForm({ ...form, niveauId: event.target.value })}><option value="">Non renseigné</option>{niveaux.map((item) => <option key={item.id} value={item.id}>{item.nom}</option>)}</select></label>
      </div>
      <div className="admin-form-actions"><button className="primary-button" type="submit" disabled={saving}>{saving ? 'Création…' : 'Créer le compte'}</button><span>Le compte est désactivé par défaut. Activez-le après sa création.</span></div>
    </form>}

    {notice && <p className="admin-notice" role="status">{notice}</p>}
    {actionError && <p className="admin-error" role="alert">{actionError}</p>}
    {importResult?.erreurs?.length > 0 && <section className="admin-import-errors" aria-labelledby="import-errors-title">
      <h2 id="import-errors-title">Lignes ignorées</h2>
      <ul>{importResult.erreurs.map((line, index) => <li key={`${index}-${line}`}>{line}</li>)}</ul>
    </section>}

    {loading && <div className="admin-state" role="status">Chargement des étudiants…</div>}
    {!loading && error && <div className="admin-state admin-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && visibleStudents.length === 0 && <div className="admin-state"><UserRound size={23} /><h2>{search ? 'Aucun étudiant trouvé' : 'Aucun étudiant'}</h2><p>{search ? 'Essayez un autre terme de recherche.' : 'Les étudiants de votre établissement apparaîtront ici.'}</p></div>}
    {!loading && !error && visibleStudents.length > 0 && <div className="admin-record-list">
      {visibleStudents.map((student) => <article className="admin-record" key={student.id}>
        <div className="admin-record-main">
          <span className={`admin-status ${student.enabled ? 'admin-status-enabled' : 'admin-status-disabled'}`}>{student.enabled ? 'Compte actif' : 'Compte désactivé'}</span>
          <h2>{student.prenom} {student.nom}</h2>
          <p>{student.username} <span>·</span> {student.email}</p>
          <p className="admin-record-summary">{referenceName(filieres, student.filiereId, 'Filière non renseignée')} · {referenceName(niveaux, student.niveauId, 'Niveau non renseigné')}</p>
        </div>
        <div className="admin-record-actions">
          <button className={student.enabled ? 'danger-button' : 'secondary-button'} onClick={() => toggleStudent(student)} disabled={changingStudentId === student.id}>
            {student.enabled ? <><UserRoundX size={15} />Désactiver</> : <><Check size={15} />Activer</>}
          </button>
        </div>
      </article>)}
    </div>}
  </section>
}

function referenceName(references, id, fallback) {
  return references.find((item) => item.id === id)?.nom || fallback
}
