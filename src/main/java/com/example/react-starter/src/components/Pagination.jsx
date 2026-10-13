export function PaginationControls({ page, pageCount, totalItems, setPage }) {
  if (totalItems <= 10) return null

  const firstItem = (page - 1) * 10 + 1
  const lastItem = Math.min(page * 10, totalItems)

  return (
    <nav className="pagination-controls" aria-label="Pagination">
      <span>Affichage {firstItem}–{lastItem} sur {totalItems}</span>
      <div>
        <button type="button" className="secondary-button" onClick={() => setPage(page - 1)} disabled={page === 1}>
          Précédent
        </button>
        <span aria-current="page">{page} / {pageCount}</span>
        <button type="button" className="secondary-button" onClick={() => setPage(page + 1)} disabled={page === pageCount}>
          Suivant
        </button>
      </div>
    </nav>
  )
}
