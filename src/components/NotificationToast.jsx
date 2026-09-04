import { useTrading } from '../context/TradingContext';
import { CheckCircle2, AlertCircle, Info } from 'lucide-react';

export default function NotificationToast() {
  const { notification } = useTrading();

  if (!notification) return null;

  const getIcon = () => {
    switch (notification.type) {
      case 'success':
        return <CheckCircle2 size={18} className="toast-icon success" />;
      case 'error':
        return <AlertCircle size={18} className="toast-icon error" />;
      default:
        return <Info size={18} className="toast-icon info" />;
    }
  };

  return (
    <div className={`notification-toast ${notification.type}`}>
      {getIcon()}
      <div className="toast-message">{notification.message}</div>
    </div>
  );
}
