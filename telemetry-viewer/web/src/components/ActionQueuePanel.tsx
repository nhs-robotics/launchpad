import type { TelemetryAction, ActionQueue } from '../types/telemetry';
import { useTheme } from '../theme';

interface ActionNodeProps {
  action: TelemetryAction;
  isActive: boolean;
  depth: number;
}

function ActionNode({ action, isActive, depth }: ActionNodeProps) {
  const { theme } = useTheme();
  const isSimultaneous = action.actionName.toLowerCase().includes('simultaneous');

  let color = theme.colorWaiting;
  if (isActive) color = theme.colorConnected;
  else if (isSimultaneous) color = theme.colorSimultaneous;

  return (
    <div style={{ paddingLeft: depth * 14 }}>
      <div style={{
        padding: '2px 6px',
        marginBottom: 2,
        background: isActive ? theme.bgActiveAction : 'transparent',
        borderLeft: '2px solid ' + (isActive ? theme.colorConnected : isSimultaneous ? theme.colorSimultaneous : theme.borderMuted),
        display: 'flex',
        alignItems: 'baseline',
        gap: 4,
        fontSize: 12,
      }}>
        <span style={{ color: theme.textTertiary, fontSize: 10, userSelect: 'none' }}>{isActive ? '▶' : '·'}</span>
        <span style={{ color }}>{action.actionName}</span>
        <span style={{ color: theme.textTertiary, fontSize: 11 }}>{action.actionParameters}</span>
        {isActive && <span style={{ color: theme.colorConnected, fontSize: 10, marginLeft: 4 }}>← active</span>}
      </div>
      {action.subActions?.map((sub, i) => (
        <ActionNode key={i} action={sub} isActive={false} depth={depth + 1} />
      ))}
    </div>
  );
}

interface Props {
  actionQueue: ActionQueue | null;
}

export function ActionQueuePanel({ actionQueue }: Props) {
  const { theme } = useTheme();

  return (
    <div style={{ background: theme.bgPanel, overflow: 'auto', padding: 12, borderRadius: 4 }}>
      <div style={{ color: theme.textTertiary, marginBottom: 8, fontSize: 11, textTransform: 'uppercase', letterSpacing: 1 }}>
        Action Queue
      </div>
      {!actionQueue ? (
        <div style={{ color: theme.textFaded, fontSize: 12 }}>No action queue</div>
      ) : actionQueue.actionQueue.length === 0 ? (
        <div style={{ color: theme.textFaded, fontSize: 12 }}>Queue empty</div>
      ) : (
        actionQueue.actionQueue.map((action, i) => (
          <ActionNode key={i} action={action} isActive={i === 0} depth={0} />
        ))
      )}
    </div>
  );
}
