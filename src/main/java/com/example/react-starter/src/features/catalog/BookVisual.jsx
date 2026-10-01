import { BookOpen } from 'lucide-react'

const tones = ['forest', 'coral', 'blue', 'gold']

function getTone(book) {
  const source = book.categorie?.nom || book.titre || ''
  const value = [...source].reduce((total, character) => total + character.codePointAt(0), 0)
  return tones[value % tones.length]
}

export default function BookVisual({ book, large = false }) {
  return <div className={`ouvrage-visual ouvrage-visual--${getTone(book)}${large ? ' ouvrage-visual--large' : ''}`}>
    <span className="ouvrage-visual-topline">BIBLIOUNIV <span>CATALOGUE</span></span>
    <BookOpen className="ouvrage-visual-icon" aria-hidden="true" />
    <strong>{book.titre}</strong>
    <span className="ouvrage-visual-author">{book.auteur}</span>
  </div>
}