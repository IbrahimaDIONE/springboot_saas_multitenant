import { FileText } from 'lucide-react'

const tones = ['forest', 'coral', 'blue', 'gold']

function getTone(memoire) {
    const source = memoire.filiere?.nom || memoire.titre || ''
    const value = [...source].reduce((total, character) => total + character.codePointAt(0), 0)
    return tones[value % tones.length]
}

export default function MemoireVisual({ memoire, large = false }) {
    return <div className={`ouvrage-visual ouvrage-visual--${getTone(memoire)}${large ? ' ouvrage-visual--large' : ''}`}>
        <span className="ouvrage-visual-topline">BIBLIOUNIV <span>MÉMOIRES</span></span>
        <FileText className="ouvrage-visual-icon" aria-hidden="true" />
        <strong>{memoire.titre}</strong>
        <span className="ouvrage-visual-author">{memoire.auteur} · {memoire.annee}</span>
    </div>
}