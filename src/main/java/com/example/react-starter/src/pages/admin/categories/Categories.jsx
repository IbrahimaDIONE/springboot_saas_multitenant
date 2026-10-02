import { useEffect, useState } from 'react'
import categoryApi from '../../../api/categoryApi'

function Categories() {
    const [categories, setCategories] = useState([])
    const [nom, setNom] = useState('')
    const [editingId, setEditingId] = useState(null)
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const loadCategories = async () => {
        try {
            setLoading(true)
            setError('')

            const data = await categoryApi.getAll()
            setCategories(data)
        } catch (err) {
            console.error(err)
            setError('Impossible de charger les catégories.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadCategories()
    }, [])

    const resetForm = () => {
        setNom('')
        setEditingId(null)
    }

    const handleSubmit = async (e) => {
        e.preventDefault()

        if (!nom.trim()) {
            setError('Le nom de la catégorie est obligatoire.')
            return
        }

        try {
            setSaving(true)
            setError('')
            setSuccess('')

            if (editingId) {
                await categoryApi.update(editingId, nom.trim())
                setSuccess('Catégorie modifiée avec succès.')
            } else {
                await categoryApi.create(nom.trim())
                setSuccess('Catégorie ajoutée avec succès.')
            }

            resetForm()
            await loadCategories()
        } catch (err) {
            console.error(err)
            setError(
                err.response?.data?.message ||
                'Une erreur est survenue lors de l’opération.'
            )
        } finally {
            setSaving(false)
        }
    }

    const handleEdit = (categorie) => {
        setEditingId(categorie.id)
        setNom(categorie.nom)
        setError('')
        setSuccess('')
    }

    const handleDelete = async (id) => {
        const confirmed = window.confirm(
            'Voulez-vous vraiment supprimer cette catégorie ?'
        )

        if (!confirmed) return

        try {
            setError('')
            setSuccess('')

            await categoryApi.delete(id)

            setSuccess('Catégorie supprimée avec succès.')
            await loadCategories()
        } catch (err) {
            console.error(err)
            setError(
                err.response?.data?.message ||
                'Impossible de supprimer cette catégorie.'
            )
        }
    }

    return (
        <div className="container-fluid py-4">
            <div className="row mb-4">
                <div className="col">
                    <h1 className="fw-bold mb-1">
                        <i className="bi bi-tags me-2"></i>
                        Gestion des catégories
                    </h1>

                    <p className="text-muted mb-0">
                        Gérez les catégories de votre établissement universitaire.
                    </p>
                </div>
            </div>

            {error && (
                <div className="alert alert-danger alert-dismissible fade show">
                    <i className="bi bi-exclamation-triangle me-2"></i>
                    {error}

                    <button
                        type="button"
                        className="btn-close"
                        onClick={() => setError('')}
                    ></button>
                </div>
            )}

            {success && (
                <div className="alert alert-success alert-dismissible fade show">
                    <i className="bi bi-check-circle me-2"></i>
                    {success}

                    <button
                        type="button"
                        className="btn-close"
                        onClick={() => setSuccess('')}
                    ></button>
                </div>
            )}

            <div className="row g-4">
                <div className="col-lg-4">
                    <div className="card border-0 shadow-sm h-100">
                        <div className="card-body p-4">
                            <h5 className="fw-bold mb-4">
                                <i
                                    className={`bi ${
                                        editingId ? 'bi-pencil-square' : 'bi-plus-circle'
                                    } me-2`}
                                ></i>

                                {editingId
                                    ? 'Modifier une catégorie'
                                    : 'Ajouter une catégorie'}
                            </h5>

                            <form onSubmit={handleSubmit}>
                                <div className="mb-3">
                                    <label className="form-label fw-semibold">
                                        Nom de la catégorie
                                    </label>

                                    <input
                                        type="text"
                                        className="form-control form-control-lg"
                                        placeholder="Ex : Informatique"
                                        value={nom}
                                        onChange={(e) => setNom(e.target.value)}
                                        maxLength={100}
                                        disabled={saving}
                                    />

                                    <div className="form-text">
                                        Maximum 100 caractères.
                                    </div>
                                </div>

                                <div className="d-flex gap-2">
                                    <button
                                        type="submit"
                                        className="btn btn-primary flex-grow-1"
                                        disabled={saving}
                                    >
                                        {saving ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm me-2"></span>
                                                Enregistrement...
                                            </>
                                        ) : (
                                            <>
                                                <i
                                                    className={`bi ${
                                                        editingId
                                                            ? 'bi-check-lg'
                                                            : 'bi-plus-lg'
                                                    } me-2`}
                                                ></i>

                                                {editingId ? 'Modifier' : 'Ajouter'}
                                            </>
                                        )}
                                    </button>

                                    {editingId && (
                                        <button
                                            type="button"
                                            className="btn btn-outline-secondary"
                                            onClick={resetForm}
                                            disabled={saving}
                                        >
                                            Annuler
                                        </button>
                                    )}
                                </div>
                            </form>
                        </div>
                    </div>
                </div>

                <div className="col-lg-8">
                    <div className="card border-0 shadow-sm">
                        <div className="card-body p-0">
                            <div className="p-4 border-bottom d-flex justify-content-between align-items-center">
                                <div>
                                    <h5 className="fw-bold mb-1">
                                        Liste des catégories
                                    </h5>

                                    <small className="text-muted">
                                        {categories.length} catégorie
                                        {categories.length > 1 ? 's' : ''}
                                    </small>
                                </div>

                                <button
                                    className="btn btn-outline-primary"
                                    onClick={loadCategories}
                                    disabled={loading}
                                >
                                    <i className="bi bi-arrow-clockwise me-2"></i>
                                    Actualiser
                                </button>
                            </div>

                            {loading ? (
                                <div className="text-center py-5">
                                    <div className="spinner-border text-primary"></div>

                                    <p className="text-muted mt-3 mb-0">
                                        Chargement des catégories...
                                    </p>
                                </div>
                            ) : categories.length === 0 ? (
                                <div className="text-center py-5 px-3">
                                    <i className="bi bi-tags fs-1 text-muted"></i>

                                    <h5 className="mt-3">
                                        Aucune catégorie
                                    </h5>

                                    <p className="text-muted">
                                        Commencez par ajouter une catégorie.
                                    </p>
                                </div>
                            ) : (
                                <div className="table-responsive">
                                    <table className="table table-hover align-middle mb-0">
                                        <thead className="table-light">
                                        <tr>
                                            <th className="px-4">#</th>
                                            <th>Nom</th>
                                            <th className="text-end px-4">Actions</th>
                                        </tr>
                                        </thead>

                                        <tbody>
                                        {categories.map((categorie, index) => (
                                            <tr key={categorie.id}>
                                                <td className="px-4 text-muted">
                                                    {index + 1}
                                                </td>

                                                <td>
                            <span className="fw-semibold">
                              {categorie.nom}
                            </span>
                                                </td>

                                                <td className="text-end px-4">
                                                    <button
                                                        className="btn btn-sm btn-outline-primary me-2"
                                                        onClick={() =>
                                                            handleEdit(categorie)
                                                        }
                                                        title="Modifier"
                                                    >
                                                        <i className="bi bi-pencil"></i>
                                                    </button>

                                                    <button
                                                        className="btn btn-sm btn-outline-danger"
                                                        onClick={() =>
                                                            handleDelete(categorie.id)
                                                        }
                                                        title="Supprimer"
                                                    >
                                                        <i className="bi bi-trash"></i>
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Categories