interface Props {
  markers: { uid: string; title: string }[];
  onRemove: (uid: string) => void;
}

export function MarkerList({ markers, onRemove }: Props) {
  if (markers.length === 0) return null;

  return (
    <section style={styles.section}>
      <h2 style={styles.sectionTitle}>
        Markers ({markers.length})
      </h2>
      {markers.map((m) => (
        <div key={m.uid} style={styles.item}>
          <div>
            <div style={styles.markerTitle}>{m.title}</div>
            <div style={styles.uid}>{m.uid.slice(0, 12)}...</div>
          </div>
          <button
            style={styles.removeBtn}
            onClick={() => onRemove(m.uid)}
          >
            Remove
          </button>
        </div>
      ))}
    </section>
  );
}

const styles: Record<string, React.CSSProperties> = {
  section: {
    marginBottom: 16,
    padding: 12,
    background: '#16213e',
    borderRadius: 8,
  },
  sectionTitle: {
    fontSize: 13,
    fontWeight: 600,
    color: '#8d99ae',
    textTransform: 'uppercase' as const,
    letterSpacing: 1,
    marginBottom: 8,
  },
  item: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '8px 0',
    borderBottom: '1px solid #1a2744',
  },
  markerTitle: {
    color: '#edf2f4',
    fontSize: 13,
    fontWeight: 500,
  },
  uid: {
    color: '#555',
    fontSize: 11,
    fontFamily: 'monospace',
  },
  removeBtn: {
    padding: '4px 12px',
    background: '#3d0000',
    color: '#ff6b6b',
    border: '1px solid #5c0000',
    borderRadius: 4,
    cursor: 'pointer',
    fontSize: 12,
  },
};
