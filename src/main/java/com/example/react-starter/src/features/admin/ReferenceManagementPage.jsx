import { useEffect, useState } from 'react'
import { Check, Pencil, Plus, RotateCcw, Tags, Trash2, X } from 'lucide-react'
import { api } from '../../api/client.js'
import './admin.css'

const referenceTypes = [
  { key: 'filieres', label: 'Filières', singular: 'Filière', endpoint: '/api/filieres', description: 'Les cursus proposés par votre établissement.' },
  { key: 'niveaux', label: 'Niveaux', singular: 'Niveau', endpoint: '/api/niveaux', description: 'Les niveaux d’étude associés aux ressources.' },
  { key: 'categories', label: 'Catégories', singular: 'Catégorie', endpoint: '/api/categories', description: 'Les catégories utilisées pour classer les ouvrages.' },
]

const emptyValues = { filieres: '', niveaux: '', categories: '' }

function errorMessage(error, fallback) {
  return error.response?.data?.message || fallback
}

export default function ReferenceManagementPage() {
  const [retryVersion, setRetryVersion] = useState(0)
  const [result, setResult] = useState({ key: '', filieres: [], niveaux: [], categories: [] })
  const [values, setValues] = useState(emptyValues)
  const [editing, setEditing] = useState(null)
  const [failure, setFailure] = useState({ key: '', message: '' })
  const [actionError, setActionError] = useState('')
  const [notice, setNotice] = useState('')
  const [busy, setBusy] = useState('')
  const requestKey = String(retryVersion)
  const references = result.key === requestKey
    ? { filieres: result.filieres, niveaux: result.niveaux, categories: result.categories }
    : { filieres: [], niveaux: [], categories: [] }
  const loading = result.key !== requestKey && failure.key !== requestKey
  const error = failure.key === requestKey ? failure.message : ''

  useEffect(() => {
    const controller = new AbortController()
    Promise.all(referenceTypes.map(({ endpoint }) => api.get(endpoint, { signal: controller.signal })))
      .then((responses) => {
        setResult({
          key: requestKey,
          filieres: responses[0].data,
          niveaux: responses[1].data,
          categories: responses[2].data,
        })
      })
      .catch((requestError) => {
        if (requestError.code !== 'ERR_CANCELED') setFailure({ key: requestKey, message: errorMessage(requestError, 'Impossible de charger les référentiels.') })
      })
    return () => controller.abort()
  }, [requestKey])

  function cancelEdit() {
    setEditing(null)
  }

  async function saveReference(event, type) {
    event.preventDefault()
    const value = editing?.key === type.key ? editing.value : values[type.key]
    if (!value.trim()) return
    setBusy(type.key)
    setActionError('')
    setNotice('')
    try {
      if (editing?.key === type.key) {
        await api.put(`${type.endpoint}/${editing.id}`, { nom: value.trim() })
        setNotice(`${type.singular} modifiée.`)
        cancelEdit()
      } else {
        await api.post(type.endpoint, { nom: value.trim() })
        setNotice(`${type.singular} ajoutée.`)
        setValues((current) => ({ ...current, [type.key]: '' }))
      }
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, `Impossible d’enregistrer ${type.label.toLocaleLowerCase()}.`))
    } finally {
      setBusy('')
    }
  }

  async function deleteReference(type, item) {
    if (!window.confirm(`Supprimer « ${item.nom} » ? Les ressources qui utilisent cette valeur peuvent empêcher la suppression.`)) return
    setBusy(`${type.key}-${item.id}`)
    setActionError('')
    setNotice('')
    try {
      await api.delete(`${type.endpoint}/${item.id}`)
      setNotice(`${type.singular} supprimée.`)
      if (editing?.id === item.id) cancelEdit()
      setRetryVersion((version) => version + 1)
    } catch (requestError) {
      setActionError(errorMessage(requestError, `Impossible de supprimer « ${item.nom} ».`))
    } finally {
      setBusy('')
    }
  }

  return <section className="content admin-page">
    <header className="admin-heading">
      <span className="eyebrow">ADMINISTRATION DE L’ÉTABLISSEMENT</span>
      <h1>Filières, niveaux et catégories</h1>
      <p>Organisez les référentiels utilisés dans le catalogue et les dossiers étudiants.</p>
    </header>

    {notice && <p className="admin-notice" role="status">{notice}</p>}
    {actionError && <p className="admin-error" role="alert">{actionError}</p>}
    {loading && <div className="admin-state" role="status">Chargement des référentiels…</div>}
    {!loading && error && <div className="admin-state admin-state-error" role="alert"><p>{error}</p><button className="secondary-button" onClick={() => setRetryVersion((version) => version + 1)}><RotateCcw size={15} />Réessayer</button></div>}
    {!loading && !error && <div className="admin-reference-grid">
      {referenceTypes.map((type) => <section className="admin-reference-card" key={type.key} aria-labelledby={`${type.key}-title`}>
        <div className="admin-reference-heading">
          <span className="admin-reference-icon"><Tags size={17} /></span>
          <div><h2 id={`${type.key}-title`}>{type.label}</h2><p>{type.description}</p></div>
        </div>
        <form className="admin-reference-form" onSubmit={(event) => saveReference(event, type)}>
          <input
            id={`${type.key}-name`}
            aria-label={editing?.key === type.key ? `Modifier ${type.label.toLocaleLowerCase()}` : `Nouvelle entrée ${type.label.toLocaleLowerCase()}`}
            value={editing?.key === type.key ? editing.value : values[type.key]}
            onChange={(event) => editing?.key === type.key
              ? setEditing({ ...editing, value: event.target.value })
              : setValues((current) => ({ ...current, [type.key]: event.target.value }))}
            placeholder={`Nom de la ${type.label.toLocaleLowerCase().replace(/s$/, '')}`}
            maxLength={100}
            required
          />
          <button className="primary-button" type="submit" disabled={busy === type.key || !((editing?.key === type.key ? editing.value : values[type.key]).trim())}>
            {editing?.key === type.key ? <><Check size={15} />Enregistrer</> : <><Plus size={15} />Ajouter</>}
          </button>
          {editing?.key === type.key && <button className="icon-button" type="button" aria-label="Annuler la modification" onClick={cancelEdit}><X size={16} /></button>}
        </form>
        <ul className="admin-reference-list">
          {references[type.key].map((item) => <li key={item.id}>
            {editing?.id === item.id
              ? <span className="admin-reference-editing">Modification en cours</span>
              : <span>{item.nom}</span>}
            <span className="admin-reference-actions">
              <button className="icon-button" type="button" aria-label={`Modifier ${item.nom}`} onClick={() => { setEditing({ key: type.key, id: item.id, value: item.nom }); setActionError('') }}><Pencil size={15} /></button>
              <button className="icon-button danger-icon" type="button" aria-label={`Supprimer ${item.nom}`} onClick={() => deleteReference(type, item)} disabled={busy === `${type.key}-${item.id}`}><Trash2 size={15} /></button>
            </span>
          </li>)}
          {references[type.key].length === 0 && <li className="admin-reference-empty">Aucune entrée pour le moment.</li>}
        </ul>
      </section>)}
    </div>}
  </section>
}
