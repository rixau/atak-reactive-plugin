import type { SelfLocation } from '../atak';

interface Props {
  location: SelfLocation | null;
}

export function LocationDisplay({ location }: Props) {
  return (
    <section style={styles.section}>
      <h2 style={styles.sectionTitle}>Self Location</h2>
      {location ? (
        <div>
          <Row label="Lat" value={location.lat.toFixed(6)} />
          <Row label="Lng" value={location.lng.toFixed(6)} />
          <Row
            label="Alt"
            value={`${(location.alt ?? 0).toFixed(1)} m`}
          />
          <Row
            label="Bearing"
            value={`${location.bearing.toFixed(0)}\u00B0`}
          />
          <Row
            label="Speed"
            value={`${location.speed.toFixed(1)} m/s`}
          />
        </div>
      ) : (
        <p style={styles.noData}>No location data</p>
      )}
    </section>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div style={styles.row}>
      <span style={styles.label}>{label}</span>
      <span style={styles.value}>{value}</span>
    </div>
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
  row: {
    display: 'flex',
    justifyContent: 'space-between',
    padding: '4px 0',
  },
  label: {
    color: '#8d99ae',
    fontSize: 13,
  },
  value: {
    color: '#edf2f4',
    fontSize: 13,
    fontFamily: 'monospace',
  },
  noData: {
    color: '#555',
    fontStyle: 'italic',
  },
};
