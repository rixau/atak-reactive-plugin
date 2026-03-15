import { useState } from 'react';
import {
  isNative,
  useSelfLocation,
  useMapEvent,
  addMarker,
  removeMarker,
  panTo,
  type MapClickEvent,
  type ItemSelectedEvent,
} from './atak';
import { LocationDisplay } from './components/LocationDisplay';
import { MarkerList } from './components/MarkerList';

export function App() {
  const location = useSelfLocation();
  const lastClick = useMapEvent<'mapClick'>('mapClick');
  const lastSelected = useMapEvent<'itemSelected'>('itemSelected');
  const [markers, setMarkers] = useState<{ uid: string; title: string }[]>([]);

  const handleDropMarker = () => {
    if (!lastClick) return;
    const title = `Pin ${markers.length + 1}`;
    const uid = addMarker({
      lat: lastClick.lat,
      lng: lastClick.lng,
      title,
    });
    if (uid) {
      setMarkers((prev) => [...prev, { uid, title }]);
    }
  };

  const handleDropAtSelf = () => {
    if (!location) return;
    const title = `Self Pin ${markers.length + 1}`;
    const uid = addMarker({
      lat: location.lat,
      lng: location.lng,
      title,
    });
    if (uid) {
      setMarkers((prev) => [...prev, { uid, title }]);
    }
  };

  const handleRemoveMarker = (uid: string) => {
    removeMarker(uid);
    setMarkers((prev) => prev.filter((m) => m.uid !== uid));
  };

  const handlePanToSelf = () => {
    if (location) {
      panTo(location.lat, location.lng);
    }
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1 style={styles.title}>Reactive Plugin</h1>
        <span style={styles.badge}>
          {isNative() ? 'ATAK' : 'MOCK'}
        </span>
      </header>

      <LocationDisplay location={location} />

      <section style={styles.section}>
        <h2 style={styles.sectionTitle}>Map Events</h2>
        <EventRow label="Last Click" event={lastClick} />
        <EventRow label="Selected" event={lastSelected} />
      </section>

      <section style={styles.section}>
        <h2 style={styles.sectionTitle}>Actions</h2>
        <div style={styles.buttonRow}>
          <button style={styles.button} onClick={handleDropAtSelf}>
            Drop at Self
          </button>
          <button
            style={{
              ...styles.button,
              opacity: lastClick ? 1 : 0.5,
            }}
            onClick={handleDropMarker}
            disabled={!lastClick}
          >
            Drop at Click
          </button>
          <button style={styles.button} onClick={handlePanToSelf}>
            Pan to Self
          </button>
        </div>
      </section>

      <MarkerList markers={markers} onRemove={handleRemoveMarker} />
    </div>
  );
}

function EventRow({
  label,
  event,
}: {
  label: string;
  event: MapClickEvent | ItemSelectedEvent | null;
}) {
  return (
    <div style={styles.row}>
      <span style={styles.label}>{label}:</span>
      <span style={styles.value}>
        {event
          ? 'title' in event
            ? `${event.title} (${event.lat?.toFixed(4)}, ${event.lng?.toFixed(4)})`
            : `${event.lat.toFixed(6)}, ${event.lng.toFixed(6)}`
          : '—'}
      </span>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    padding: 16,
    maxWidth: 480,
    margin: '0 auto',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  title: {
    fontSize: 20,
    fontWeight: 700,
    color: '#fff',
  },
  badge: {
    fontSize: 11,
    fontWeight: 600,
    padding: '2px 8px',
    borderRadius: 4,
    background: '#2d6a4f',
    color: '#d8f3dc',
  },
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
  buttonRow: {
    display: 'flex',
    gap: 8,
    flexWrap: 'wrap' as const,
  },
  button: {
    padding: '8px 16px',
    background: '#0f3460',
    color: '#e0e0e0',
    border: '1px solid #1a4a7a',
    borderRadius: 6,
    cursor: 'pointer',
    fontSize: 13,
    fontWeight: 500,
  },
};
