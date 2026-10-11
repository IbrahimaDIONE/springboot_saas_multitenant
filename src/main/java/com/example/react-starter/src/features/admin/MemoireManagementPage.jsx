import { useEffect, useMemo, useState } from 'react'
import { Archive, Download, FilePlus2, FileText, Pencil, Plus, RotateCcw, Search, Trash2, X } from 'lucide-react'
import { api } from '../../api/client.js'
import './admin.css'

const emptyMemoire = { titre: '', auteur: '', encadreur: '', resume: '', annee: new Date().getFullYear(), filiereId: '', niveauId: '' }

function errorMessage(error, fallback) {
  return error.response?.data?.message || fallback
}

export default function MemoireManagementPage() {
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', memoires: [], filieres: [], niveaux: [] })
  const [failure, setFailure] = useState({ key: '', message: '' })
  const [actionError, setActionError] = useState('')
  const [notice, setNotice] = useState('')
  const [search, setSearch] = useState('')
  const [showArchives, setShowArchives] = useState(false)
  const [formOpen, setFormOpen] = useState(false)
  const [editingMemoire, setEditingMemoire] = useState(null)
  const [form, setForm] = useState(emptyMemoire)
  const [saving, setSaving] = useState(false)
  const [uploadingId, setUploadingId] = useState('')
  const requestKey = JSON.stringify([showArchives, retryVersion])
  const currentResult = result.key === requestKey ? result : { memoires: [], filieres: [], niveaux: [] }
  const { memoires, filieres, niveaux } = currentResult
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      api.get(showArchives ? '/api/memoires/archives' : '/api/memoires', { signal: controller.signal }),
      api.get('/api/filieres', { signal: controller.signal }),
      api.get('/api/niveaux', { signal: controller.signal }),
    ])
      .then(([memoireResponse, filiereResponse, niveauResponse]) => {
        setResult({
          key: requestKey,
          memoires: memoireResponse.data,
          filieres: filiereResponse.data,
          niveaux: niveauResponse.data,
        })
      })
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: errorMessage(requestError, 'Impossible de charger les mémoires.') })
      })
    return () => controller.abort()
  }, [requestKey, showArchives])

  const visibleMemoires = useMemo(() => {
    const term = search.trim().toLocaleLowerCase()
    if (!term) return memoires
    return memoires.filter((memoire) => [memoire.titre, memoire.auteur, memoire.encadreur, memoire.filiere?.nom]
      .some((value) => value?.toLocaleLowerCase().includes(term)))
  }, [memoires, search])

  function clearForm() {
    setFormOpen(false)
    setEditingMemoire(null)
    setForm(emptyMemoire)
    setActionError('')
  }

  function startCreate() {
    setFormOpen(true)
    setEditingMemoire(null)
    setForm(emptyMemoire)
    setActionError('')
    setNotice('')
  }

  function startEdit(memoire) {
    setFormOpen(true)
    setEditingMemoire(memoire)
    setForm({
      titre: memoire.titre,
      auteur: memoire.auteur,
      encadreur: memoire.encadreur,
      resume: memoire.resume,
      annee: memoire.annee,
      filiereId: memoire.filiere?.id || '',
      niveauId: memoire.niveau?.id || '',
    })
    setActionError('')
    setNotice('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setActionError('')
    setNotice('')
    const payload = { ...form, annee: Number(form.annee) }
    try {
      if (editingMemoire) {
        await api.put(`/api/memoires/${editingMemoire.id}`, payload)
        setNotice('Le mémoire a été mis à jour.')
      } else {
        await api.post('/api/memoires', payload)
        setNotice('Le mémoire a été ajouté.')
      }
      clearForm()
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible d’enregistrer ce mémoire.'))
    } finally {
      setSaving(false)
    }
  }

  async function changeStatus(memoire, action) {
    setActionError('')
    setNotice('')
    try {
      await api.patch(`/api/memoires/${memoire.id}/${action}`)
      setNotice(action === 'archiver' ? 'Le mémoire a été archivé.' : 'Le mémoire a été réactivé.')
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de modifier le statut de ce mémoire.'))
    }
  }

  async function deleteMemoire(memoire) {
    if (!window.confirm(`Supprimer définitivement « ${memoire.titre} » et ses fichiers associés ?`)) return
    setActionError('')
    setNotice('')
    try {
      await api.delete(`/api/memoires/${memoire.id}`)
      setNotice('Le mémoire et ses fichiers ont été supprimés.')
      if (editingMemoire?.id === memoire.id) clearForm()
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de supprimer ce mémoire.'))
    }
  }

  async function uploadFile(memoire, file) {
    if (!file) return
    setActionError('')
    setNotice('')
    if (!file.name.toLowerCase().endsWith('.pdf')) {
      setActionError('Sélectionnez un fichier PDF.')
      return
    }
    const formData = new FormData()
    formData.append('file', file)
    setUploadingId(memoire.id)
    try {
      await api.post(`/api/memoires/${memoire.id}/fichiers`, formData)
      setNotice(`Le document a été ajouté au mémoire « ${memoire.titre} ».`)
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Le téléversement du document a échoué.'))
    } finally {
      setUploadingId('')
    }
  }

  async function deleteFile(memoire, file) {
    if (!window.confirm(`Supprimer le fichier « ${file.nomOriginal} » ?`)) return
    setActionError('')
    setNotice('')
    try {
      await api.delete(`/api/memoires/${memoire.id}/fichiers/${file.id}`)
      setNotice('Le fichier a été supprimé.')
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de supprimer ce fichier.'))
    }
  }

  async function toggleFileAvailability(memoire, file) {
    setActionError('')
    setNotice('')
    try {
      await api.patch(`/api/memoires/${memoire.id}/fichiers/${file.id}/disponibilite`, null, {
        params: { disponible: !file.disponible },
      })
      setNotice(file.disponible ? 'Le fichier est maintenant indisponible pour les étudiants.' : 'Le fichier est de nouveau disponible.')
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de modifier la disponibilité du fichier.'))
    }
  }

  async function downloadFile(memoire, file) {
    setActionError('')
    try {
      const { data } = await api.get(`/api/memoires/${memoire.id}/fichiers/${file.id}`, { responseType: 'blob' })
      const url = URL.createObjectURL(data)
      const link = document.createElement('a')
      link.href = url
      link.download = file.nomOriginal
      document.body.append(link)
      link.click()
      link.remove()
      window.setTimeout(() => URL.revokeObjectURL(url), 1000)
    } catch (requestError) {
      setActionError(errorMessage(requestError, 'Impossible de télécharger ce fichier.'))
    }
  }

  return <section className="content admin-page">
    <header className="admin-heading">
      <span className="eyebrow">ADMINISTRATION DE L’ÉTABLISSEMENT</span>
      <h1>Gestion des mémoires</h1>
      <p>Référencez les travaux de recherche et gérez leurs documents PDF.</p>
    </header>

    <div className="admin-toolbar">
      <div className="admin-tabs" aria-label="Filtrer les mémoires">
        <button type="button" className={!showArchives ? 'selected' : ''} onClick={() => { setShowArchives(false); setSearch(''); clearForm() }}>Mémoires actifs</button>
        <button type="button" className={showArchives ? 'selected' : ''} onClick={() => { setShowArchives(true); setSearch(''); clearForm() }}>Archives</button>
      </div>
      <label className="admin-search"><Search size={16} aria-hidden="true" /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Rechercher un titre, auteur ou encadreur" aria-label="Rechercher un mémoire" /></label>
      {!showArchives && <button className="primary-button" type="button" onClick={startCreate}><Plus size={16} />Ajouter un mémoire</button>}
    </div>

    {!showArchives && formOpen && <form className="admin-form" onSubmit={handleSubmit}>
      <div className="admin-form-heading">
        <div><span className="eyebrow">{editingMemoire ? 'MODIFICATION' : 'NOUVELLE RESSOURCE'}</span><h2>{editingMemoire ? 'Modifier un mémoire' : 'Ajouter un mémoire'}</h2></div>
        <button className="icon-button" type="button" aria-label={editingMemoire ? 'Annuler la modification' : 'Fermer le formulaire'} onClick={clearForm}><X size={16} /></button>
      </div>
      <div className="admin-fields">
        <label>Titre<input value={form.titre} onChange={(event) => setForm({ ...form, titre: event.target.value })} maxLength={255} required /></label>
        <label>Auteur<input value={form.auteur} onChange={(event) => setForm({ ...form, auteur: event.target.value })} maxLength={150} required /></label>
        <label>Encadreur<input value={form.encadreur} onChange={(event) => setForm({ ...form, encadreur: event.target.value })} maxLength={150} required /></label>
        <label>Année<input type="number" value={form.annee} min={2000} onChange={(event) => setForm({ ...form, annee: event.target.value })} required /></label>
        <label>Filière<select value={form.filiereId} onChange={(event) => setForm({ ...form, filiereId: event.target.value })} required><option value="">Choisir une filière</option>{filieres.map((item) => <option key={item.id} value={item.id}>{item.nom}</option>)}</select></label>
        <label>Niveau<select value={form.niveauId} onChange={(event) => setForm({ ...form, niveauId: event.target.value })} required><option value="">Choisir un niveau</option>{niveaux.map((item) => <option key={item.id} value={item.id}>{item.nom}</option>)}</select></label>
        <label className="admin-field-wide">Résumé<textarea value={form.resume} onChange={(event) => setForm({ ...form, resume: event.target.value })} rows={3} required /></label>
      </div>
      <div className="admin-form-actions"><button className="primary-button" type="submit" disabled={saving}>{saving ? 'Enregistrement…' : editingMemoire ? 'Enregistrer les modifications' : 'Ajouter le mémoire'}</button></div>
    </form>}

    {notice && <p className="admin-notice" role="status">{notice}</p>}
    {actionError && <p className="admin-error" role="alert">{actionError}</p>}
    {loading && <div className="admin-state" role="status">Chargement des mémoires…</div>}
    {!loading && error && <div className="admin-state admin-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && visibleMemoires.length === 0 && <div className="admin-state"><FileText size={23} /><h2>{showArchives ? 'Aucun mémoire archivé' : 'Aucun mémoire trouvé'}</h2><p>{search ? 'Essayez un autre terme de recherche.' : 'Les mémoires de votre établissement apparaîtront ici.'}</p></div>}
    {!loading && !error && visibleMemoires.length > 0 && <div className="admin-record-list">
      {visibleMemoires.map((memoire) => <article className="admin-record admin-record-stacked" key={memoire.id}>
        <div className="admin-record-main">
          <span className="admin-record-eyebrow">{memoire.annee} · {memoire.filiere?.nom || 'Filière non définie'} · {memoire.niveau?.nom || 'Niveau non défini'}</span>
          <h2>{memoire.titre}</h2>
          <p>{memoire.auteur} <span>·</span> Encadrement : {memoire.encadreur}</p>
          {memoire.resume && <p className="admin-record-summary">{memoire.resume}</p>}
        </div>
        <div className="admin-record-actions">
          {showArchives
            ? <button className="secondary-button" onClick={() => changeStatus(memoire, 'reactiver')}><RotateCcw size={15} />Réactiver</button>
            : <><button className="secondary-button" onClick={() => startEdit(memoire)}><Pencil size={15} />Modifier</button><button className="secondary-button" onClick={() => changeStatus(memoire, 'archiver')}><Archive size={15} />Archiver</button></>}
          <button className="danger-button" onClick={() => deleteMemoire(memoire)}><Trash2 size={15} />Supprimer</button>
        </div>
        <div className="admin-files">
          <div className="admin-files-heading"><h3>Documents associés</h3><span>{memoire.fichiers?.length || 0} fichier{memoire.fichiers?.length === 1 ? '' : 's'}</span></div>
          {memoire.fichiers?.length > 0 && <ul className="admin-file-list">
            {memoire.fichiers.map((file) => <li key={file.id}>
              <span className="admin-file-name"><FileText size={15} /><span><strong>{file.nomOriginal}</strong><small>{formatFileSize(file.tailleOctets)} · {file.disponible ? 'Disponible' : 'Indisponible'}</small></span></span>
              <div className="admin-file-actions">
                <button className="secondary-button" type="button" onClick={() => downloadFile(memoire, file)} disabled={!file.disponible}><Download size={14} />Télécharger</button>
                <button className="secondary-button" type="button" onClick={() => toggleFileAvailability(memoire, file)}>{file.disponible ? 'Rendre indisponible' : 'Rendre disponible'}</button>
                <button className="danger-button" type="button" onClick={() => deleteFile(memoire, file)}><Trash2 size={14} />Supprimer</button>
              </div>
            </li>)}
          </ul>}
          {!showArchives && <label className="admin-upload">
            <FilePlus2 size={15} />{uploadingId === memoire.id ? 'Téléversement…' : 'Ajouter un PDF'}
            <input type="file" accept=".pdf,application/pdf" aria-label={`Ajouter un PDF au mémoire ${memoire.titre}`} disabled={Boolean(uploadingId)} onChange={(event) => { const input = event.currentTarget; uploadFile(memoire, input.files?.[0]).finally(() => { input.value = '' }) }} />
          </label>}
        </div>
      </article>)}
    </div>}
  </section>
}

function formatFileSize(size) {
  if (!Number.isFinite(size) || size < 0) return 'Taille inconnue'
  if (size < 1024) return `${size} o`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} Ko`
  return `${(size / (1024 * 1024)).toFixed(1)} Mo`
}
