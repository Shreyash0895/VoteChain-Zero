/**
 * The left-side panel on auth pages (and similar) — fills what would
 * otherwise be dead space with the brand voice and a textural motif that
 * reinforces "ledger", without faking live data (no invented block numbers
 * or vote counts here — that's what the real Chain Explorer is for).
 */
export default function BrandPanel({ eyebrow, title, description }) {
  return (
    <div className="hidden md:flex flex-col justify-between border-r border-rule pr-12 py-4">
      <div>
        {eyebrow && <p className="font-mono text-xs text-brass mb-4">{eyebrow}</p>}
        <h2 className="font-serif text-4xl font-semibold leading-tight mb-5">{title}</h2>
        {description && <p className="text-paper-dim leading-relaxed max-w-sm">{description}</p>}
      </div>

      <div className="space-y-0">
        {['ENTRY', 'SIGNED', 'HASHED', 'CHAINED', 'VERIFIED'].map((label, i) => (
          <div key={label} className="flex items-center justify-between py-3 border-t border-rule first:border-t-0">
            <span className="font-mono text-[11px] tracking-widest text-paper-dim/40">
              {String(i + 1).padStart(2, '0')}
            </span>
            <span className="font-mono text-[11px] tracking-widest text-paper-dim/40">{label}</span>
          </div>
        ))}
      </div>
    </div>
  )
}