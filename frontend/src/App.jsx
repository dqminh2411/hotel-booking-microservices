import { useEffect, useMemo, useRef, useState } from 'react';

const API_BASE_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

const HOTEL = {
  id: 'HT-001',
  name: 'Marriott Hanoi',
  description:
    'Khach san 5 sao sang trong toa lac tai trung tam Ha Noi, cach Ho Hoan Kiem 5 phut di bo.',
  address: '12 Phan Chu Trinh, Hoan Kiem, Hanoi',
  imageUrl: 'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/0e/2d/28/dc/pool.jpg?w=700&h=-1&s=1'
};

const USER = {
  id: 'US-001',
  name: 'Nguyen Duc Lam',
  email: 'nguyenduclam478@gmail.com',
  phone: '0901234561'
};

const ROOM_TYPES = [
  {
    id: 'RT-001',
    name: 'Superior Room',
    description: 'Phong tieu chuan hien dai voi view thanh pho, day du tien nghi.',
    maxGuests: 2,
    bedCounts: 1,
    basePricePerNight: 1500000,
    availableQuantity: 15,
    area: 28,
    imageUrl: 'https://wallpaperaccess.com/full/2690753.jpg'
  },
  {
    id: 'RT-002',
    name: 'Deluxe Room',
    description: 'Phong rong hon voi view ho Hoan Kiem, noi that cao cap.',
    maxGuests: 2,
    bedCounts: 1,
    basePricePerNight: 2200000,
    availableQuantity: 10,
    area: 35,
    imageUrl:
      'https://image.nuprop.my/small_light(da=l,ds=s,cc=f5f5f5,autoorient=y,progressive=y,rmprof=y,of=jpg,cw=800,ch=600,dh=600)/nuprop-production/2805a0add39a76855e0fe0abf2b45f7f_800_600.jpg'
  },
  {
    id: 'RT-003',
    name: 'Junior Suite',
    description: 'Suite sang trong voi phong khach rieng biet va bon tam dung.',
    maxGuests: 3,
    bedCounts: 1,
    basePricePerNight: 3800000,
    availableQuantity: 3,
    area: 55,
    imageUrl:
      'https://image.nuprop.my/small_light(da=l,ds=s,cc=f5f5f5,autoorient=y,progressive=y,rmprof=y,of=jpg,cw=800,ch=600,dh=600)/nuprop-production/2805a0add39a76855e0fe0abf2b45f7f_800_600.jpg'
  }
];

const today = new Date().toISOString().slice(0, 10);
const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 10);

function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value || 0);
}

function nightsBetween(checkin, checkout) {
  if (!checkin || !checkout) {
    return 0;
  }
  const c1 = new Date(checkin + 'T00:00:00');
  const c2 = new Date(checkout + 'T00:00:00');
  const diff = Math.floor((c2 - c1) / 86400000);
  return diff > 0 ? diff : 0;
}

function createFakeToken() {
  return `tok_${Math.random().toString(36).slice(2, 12)}`;
}

function createIdempotencyKey() {
  if (window.crypto && window.crypto.randomUUID) {
    return window.crypto.randomUUID();
  }
  return `idemp_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;
}

function Spinner() {
  return <div className="h-8 w-8 animate-spin rounded-full border-4 border-slate-300 border-t-blue-600" />;
}

export default function App() {
  const [checkin, setCheckin] = useState(tomorrow);
  const [checkout, setCheckout] = useState('');
  const [numAdults, setNumAdults] = useState(2);
  const [payment, setPayment] = useState({ cardNumber: '', cardholderName: '', expiryDate: '', cvv: '' });
  const [selectedRooms, setSelectedRooms] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [submitLoading, setSubmitLoading] = useState(false);
  const [bookingId, setBookingId] = useState('');
  const [bookingStatus, setBookingStatus] = useState('IDLE');
  const [pollError, setPollError] = useState('');
  const [bookingDetails, setBookingDetails] = useState(null);
  const pollRef = useRef(null);

  const nights = useMemo(() => nightsBetween(checkin, checkout), [checkin, checkout]);

  const selectedRoomItems = useMemo(
    () =>
      ROOM_TYPES.filter((room) => Number(selectedRooms[room.id] || 0) > 0).map((room) => ({
        roomTypeId: room.id,
        quantity: Number(selectedRooms[room.id]),
        price: room.basePricePerNight,
        name: room.name
      })),
    [selectedRooms]
  );

  const totalAmount = useMemo(
    () => selectedRoomItems.reduce((sum, item) => sum + item.price * item.quantity * nights, 0),
    [selectedRoomItems, nights]
  );

  useEffect(() => {
    return () => {
      if (pollRef.current) {
        clearInterval(pollRef.current);
      }
    };
  }, []);

  async function pollBooking(currentBookingId) {
    try {
      const response = await fetch(`${API_BASE_URL}/bookings/${currentBookingId}`);
      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(data.message || 'Khong the lay trang thai booking.');
      }

      const status = (data.status || '').toUpperCase();
      setBookingStatus(status || 'PENDING');
      setBookingDetails(data);

      if (status === 'CONFIRMED' || status === 'CANCELLED' || status === 'FAILED') {
        if (pollRef.current) {
          clearInterval(pollRef.current);
        }
      }
    } catch (error) {
      setPollError(error.message);
      if (pollRef.current) {
        clearInterval(pollRef.current);
      }
    }
  }

  function validateForm() {
    if (!checkin || !checkout) {
      return 'Vui long chon checkin va checkout.';
    }
    if (new Date(checkin) < new Date(today)) {
      return 'Checkin phai la ngay hien tai hoac tuong lai.';
    }
    if (nights <= 0) {
      return 'Checkout phai sau checkin it nhat 1 ngay.';
    }
    if (!selectedRoomItems.length) {
      return 'Vui long chon it nhat 1 loai phong.';
    }
    if (!payment.cardNumber || !payment.cardholderName || !payment.expiryDate || !payment.cvv) {
      return 'Vui long nhap day du thong tin the thanh toan.';
    }
    return '';
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError('');
    setPollError('');

    const error = validateForm();
    if (error) {
      setSubmitError(error);
      return;
    }

    const payload = {
      userId: USER.id,
      hotelId: HOTEL.id,
      roomTypeList: selectedRoomItems.map((room) => ({
        roomTypeId: room.roomTypeId,
        quantity: room.quantity,
        price: room.price
      })),
      checkin,
      checkout,
      numAdults: Number(numAdults),
      totalAmount,
      currency: 'VND',
      paymentMethod: 'CREDIT_CARD',
      paymentToken: createFakeToken(),
      idempotencyKey: createIdempotencyKey()
    };

    setSubmitLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/place-booking`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const data = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(data.message || 'Dat phong that bai.');
      }

      const createdBookingId = data.bookingId;
      if (!createdBookingId) {
        throw new Error('Khong nhan duoc bookingId tu backend.');
      }

      setBookingId(createdBookingId);
      setBookingStatus('PENDING');
      setBookingDetails({
        bookingId: createdBookingId,
        status: 'PENDING',
        checkin,
        checkout,
        totalAmount,
        roomTypeList: selectedRoomItems,
        hotel: { name: HOTEL.name }
      });

      await pollBooking(createdBookingId);
      pollRef.current = setInterval(() => {
        pollBooking(createdBookingId);
      }, 2000);
    } catch (submitErr) {
      setSubmitError(submitErr.message);
    } finally {
      setSubmitLoading(false);
    }
  }

  return (
    <main className="mx-auto max-w-5xl p-4 md:p-8">
      <h1 className="mb-6 text-3xl font-bold text-slate-800">Demo Dat Phong Khach San</h1>

      <section className="mb-6 overflow-hidden rounded-xl bg-white shadow">
        <img className="h-60 w-full object-cover" src={HOTEL.imageUrl} alt={HOTEL.name} />
        <div className="p-5">
          <p className="text-sm text-slate-500">ID: {HOTEL.id}</p>
          <h2 className="mt-1 text-2xl font-semibold">{HOTEL.name}</h2>
          <p className="mt-2 text-slate-600">{HOTEL.description}</p>
          <p className="mt-2 text-sm text-slate-500">{HOTEL.address}</p>
        </div>
      </section>

      <form onSubmit={handleSubmit} className="space-y-6 rounded-xl bg-white p-5 shadow">
        <section>
          <h3 className="mb-3 text-lg font-semibold">Thong tin nguoi dat</h3>
          <div className="grid gap-3 md:grid-cols-2">
            <input className="rounded border bg-slate-100 p-2" value={`${USER.id} - ${USER.name}`} readOnly />
            <input className="rounded border bg-slate-100 p-2" value={USER.email} readOnly />
            <input className="rounded border bg-slate-100 p-2 md:col-span-2" value={USER.phone} readOnly />
          </div>
        </section>

        <section>
          <h3 className="mb-3 text-lg font-semibold">Thong tin dat phong</h3>
          <div className="mb-4 grid gap-3 md:grid-cols-3">
            <label className="text-sm">
              Checkin
              <input
                type="date"
                min={today}
                value={checkin}
                onChange={(e) => setCheckin(e.target.value)}
                className="mt-1 w-full rounded border p-2"
                required
              />
            </label>
            <label className="text-sm">
              Checkout
              <input
                type="date"
                min={checkin || today}
                value={checkout}
                onChange={(e) => setCheckout(e.target.value)}
                className="mt-1 w-full rounded border p-2"
                required
              />
            </label>
            <label className="text-sm">
              So nguoi lon
              <input
                type="number"
                min="1"
                value={numAdults}
                onChange={(e) => setNumAdults(e.target.value)}
                className="mt-1 w-full rounded border p-2"
                required
              />
            </label>
          </div>

          <div className="space-y-3">
            {ROOM_TYPES.map((room) => {
              const quantity = Number(selectedRooms[room.id] || 0);
              return (
                <div key={room.id} className="rounded-lg border p-3">
                  <div className="mb-2 flex items-start justify-between gap-3">
                    <div>
                      <p className="font-medium">{room.name}</p>
                      <p className="text-sm text-slate-500">{room.description}</p>
                      <p className="mt-1 text-xs text-slate-500">
                        {room.maxGuests} khach | {room.bedCounts} giuong | {room.area} m2 | Con {room.availableQuantity}
                      </p>
                    </div>
                    <span className="text-sm font-semibold text-blue-700">{formatCurrency(room.basePricePerNight)} / dem</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <label className="text-sm">So luong:</label>
                    <input
                      type="number"
                      min="0"
                      max={room.availableQuantity}
                      value={quantity}
                      onChange={(e) =>
                        setSelectedRooms((prev) => ({
                          ...prev,
                          [room.id]: e.target.value
                        }))
                      }
                      className="w-24 rounded border p-2"
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        <section>
          <h3 className="mb-3 text-lg font-semibold">Thanh toan (demo)</h3>
          <div className="grid gap-3 md:grid-cols-2">
            <input
              className="rounded border p-2"
              placeholder="So the"
              value={payment.cardNumber}
              onChange={(e) => setPayment((prev) => ({ ...prev, cardNumber: e.target.value }))}
            />
            <input
              className="rounded border p-2"
              placeholder="Ten chu the"
              value={payment.cardholderName}
              onChange={(e) => setPayment((prev) => ({ ...prev, cardholderName: e.target.value }))}
            />
            <input
              className="rounded border p-2"
              placeholder="MM/YY"
              value={payment.expiryDate}
              onChange={(e) => setPayment((prev) => ({ ...prev, expiryDate: e.target.value }))}
            />
            <input
              className="rounded border p-2"
              placeholder="CVV"
              value={payment.cvv}
              onChange={(e) => setPayment((prev) => ({ ...prev, cvv: e.target.value }))}
            />
          </div>
        </section>

        <section className="rounded-lg bg-slate-50 p-4">
          <p className="text-sm text-slate-500">Currency: VND</p>
          <p className="mt-2 text-xl font-bold text-slate-800">Tong tien: {formatCurrency(totalAmount)}</p>
          <p className="text-sm text-slate-500">So dem: {nights}</p>
        </section>

        {submitError && <p className="rounded bg-red-50 p-3 text-sm text-red-600">{submitError}</p>}

        <button
          type="submit"
          disabled={submitLoading}
          className="inline-flex items-center rounded bg-blue-600 px-5 py-2.5 font-medium text-white hover:bg-blue-700 disabled:opacity-60"
        >
          {submitLoading ? 'Dang gui...' : 'Book Now'}
        </button>
      </form>

      {bookingId && (
        <section className="mt-6 rounded-xl bg-white p-5 shadow">
          <h3 className="mb-3 text-lg font-semibold">Trang thai booking: {bookingStatus}</h3>

          {bookingStatus === 'PENDING' && (
            <div className="flex items-center gap-3 text-blue-700">
              <Spinner />
              <p>He thong dang xu ly booking. Dang polling moi 2 giay...</p>
            </div>
          )}

          {bookingStatus === 'CONFIRMED' && (
            <div className="rounded bg-emerald-50 p-4 text-emerald-700">
              <p className="font-semibold">Dat phong thanh cong!</p>
              <p>Booking ID: {bookingDetails?.bookingId || bookingId}</p>
              <p>Khach san: {bookingDetails?.hotel?.name || HOTEL.name}</p>
              <p>
                Checkin - Checkout: {bookingDetails?.checkin || checkin} - {bookingDetails?.checkout || checkout}
              </p>
              <p>Tong tien: {formatCurrency(bookingDetails?.totalAmount || totalAmount)}</p>
            </div>
          )}

          {(bookingStatus === 'CANCELLED' || bookingStatus === 'FAILED') && (
            <div className="rounded bg-red-50 p-4 text-red-700">
              <p className="font-semibold">Dat phong that bai.</p>
              <p>Booking ID: {bookingDetails?.bookingId || bookingId}</p>
              <p>Trang thai: {bookingStatus}</p>
            </div>
          )}

          {pollError && <p className="mt-3 rounded bg-red-50 p-3 text-sm text-red-600">{pollError}</p>}
        </section>
      )}
    </main>
  );
}

