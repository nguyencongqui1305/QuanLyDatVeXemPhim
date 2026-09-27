// =====================================================
// UTC CINEMA - MAIN JAVASCRIPT
// =====================================================

// ---- SEAT SELECTION ----
const SeatMap = {
  selectedSeats: [],
  basePrice: 0,
  seatPrices: {},

  init(basePrice) {
    this.basePrice = basePrice;
    document.querySelectorAll('.seat:not(:disabled)').forEach(seat => {
      seat.addEventListener('click', () => this.toggleSeat(seat));
    });
    this.updateSummary();
  },

  toggleSeat(seat) {
    const maGhe = seat.dataset.maGhe;
    const loaiGhe = seat.dataset.loaiGhe || 'Thường';
    const price = this.calculatePrice(loaiGhe);

    if (seat.classList.contains('seat-selected')) {
      seat.classList.remove('seat-selected');
      seat.classList.add('seat-' + this.getClass(loaiGhe));
      this.selectedSeats = this.selectedSeats.filter(s => s !== maGhe);
      delete this.seatPrices[maGhe];
    } else {
      seat.classList.remove('seat-' + this.getClass(loaiGhe));
      seat.classList.add('seat-selected');
      this.selectedSeats.push(maGhe);
      this.seatPrices[maGhe] = price;
    }
    this.updateSummary();
  },

  getClass(loaiGhe) {
    if (loaiGhe === 'VIP') return 'vip';
    if (loaiGhe === 'Đôi') return 'doi';
    return 'thuong';
  },

  calculatePrice(loaiGhe) {
    if (loaiGhe === 'VIP') return Math.round(this.basePrice * 1.3);
    if (loaiGhe === 'Đôi') return Math.round(this.basePrice * 1.5);
    return this.basePrice;
  },

  updateSummary() {
    const total = Object.values(this.seatPrices).reduce((a, b) => a + b, 0);
    const count = this.selectedSeats.length;

    const countEl = document.getElementById('selectedCount');
    const totalEl = document.getElementById('totalPrice');
    const listEl = document.getElementById('selectedSeatsList');
    const submitBtn = document.getElementById('submitBtn');
    const hiddenInput = document.getElementById('maGhesInput');

    if (countEl) countEl.textContent = count;
    if (totalEl) totalEl.textContent = formatMoney(total);
    if (hiddenInput) hiddenInput.value = this.selectedSeats.join(',');
    if (submitBtn) submitBtn.disabled = count === 0;

    if (listEl) {
      if (count === 0) {
        listEl.innerHTML = '<span class="text-muted-utc" style="font-size:0.85rem">Chưa chọn ghế nào</span>';
      } else {
        listEl.innerHTML = this.selectedSeats.map(maGhe => {
          const seat = document.querySelector(`[data-ma-ghe="${maGhe}"]`);
          const hang = seat?.dataset.hang || '';
          const so = seat?.dataset.so || '';
          const loai = seat?.dataset.loaiGhe || 'Thường';
          const price = this.seatPrices[maGhe] || 0;
          return `<div class="booking-info-row">
            <span class="booking-info-label">${hang}${so} <small class="text-muted-utc">(${loai})</small></span>
            <span class="booking-info-value price-text">${formatMoney(price)}</span>
          </div>`;
        }).join('');
      }
    }
  }
};

// ---- COUNTDOWN TIMER ----
function startCountdown(seconds, elementId) {
  const el = document.getElementById(elementId);
  if (!el) return;
  let remaining = seconds;
  const interval = setInterval(() => {
    remaining--;
    const m = Math.floor(remaining / 60).toString().padStart(2, '0');
    const s = (remaining % 60).toString().padStart(2, '0');
    el.textContent = `${m}:${s}`;
    if (remaining <= 60) el.style.color = '#ff4d4d';
    if (remaining <= 0) {
      clearInterval(interval);
      showToast('Thời gian giữ ghế đã hết! Vui lòng chọn lại.', 'error');
      setTimeout(() => window.location.reload(), 2000);
    }
  }, 1000);
}

// ---- TOAST NOTIFICATION ----
function showToast(message, type = 'success') {
  const container = document.getElementById('toastContainer') || createToastContainer();
  const toastEl = document.createElement('div');
  const colors = { success: '#46d369', error: '#e50914', warning: '#f5c518', info: '#5b9bd5' };
  const icons = { success: 'check-circle', error: 'exclamation-circle', warning: 'exclamation-triangle', info: 'info-circle' };

  toastEl.innerHTML = `
    <div style="background:#1a1a1a;border:1px solid ${colors[type]}30;border-left:3px solid ${colors[type]};
      border-radius:10px;padding:14px 18px;display:flex;align-items:center;gap:12px;
      min-width:280px;max-width:360px;box-shadow:0 8px 24px rgba(0,0,0,0.5);
      animation:fadeInUp 0.3s ease;margin-bottom:8px;">
      <i class="fas fa-${icons[type]}" style="color:${colors[type]};font-size:1.1rem;flex-shrink:0"></i>
      <span style="color:#fff;font-size:0.88rem;font-weight:500;line-height:1.4">${message}</span>
    </div>`;

  container.appendChild(toastEl);
  setTimeout(() => { toastEl.style.opacity = '0'; toastEl.style.transform = 'translateX(100px)'; toastEl.style.transition = '0.3s'; setTimeout(() => toastEl.remove(), 300); }, 4000);
}

function createToastContainer() {
  const c = document.createElement('div');
  c.id = 'toastContainer';
  c.className = 'toast-container-utc';
  document.body.appendChild(c);
  return c;
}

// ---- MONEY FORMAT ----
function formatMoney(amount) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

// ---- PROMO CODE CALCULATION ----
function applyPromo(baseTotal, loaiGiam, giaTri) {
  if (loaiGiam === 'PERCENT') return Math.round(baseTotal * giaTri / 100);
  if (loaiGiam === 'AMOUNT') return Math.min(giaTri, baseTotal);
  return 0;
}

// ---- SCHEDULE DATE PICKER ----
function initDatePicker() {
  const pills = document.querySelectorAll('.date-pill');
  pills.forEach(pill => {
    pill.addEventListener('click', function() {
      pills.forEach(p => p.classList.remove('active'));
      this.classList.add('active');
      loadSchedule(this.dataset.ngay);
    });
  });
  if (pills.length > 0) pills[0].click();
}

function loadSchedule(ngay) {
  const maPhim = document.getElementById('maPhim')?.value;
  const maRap = document.getElementById('maRap')?.value || document.getElementById('maRapHidden')?.value;
  if (!maPhim || !maRap) return;

  fetch(`/phim/lich-chieu?maPhim=${maPhim}&maRap=${maRap}&ngay=${ngay}`)
    .then(r => r.json())
    .then(data => {
      const container = document.getElementById('showtimeContainer');
      if (!container) return;
      if (!data || data.length === 0) {
        container.innerHTML = '<p class="text-muted-utc text-center py-3"><i class="fas fa-calendar-times me-1"></i>Không có suất chiếu nào</p>';
        return;
      }
      container.innerHTML = data.map(lc => `
        <a href="/dat-ve/chon-ghe/${lc.maLich}" class="showtime-btn">
          <i class="fas fa-clock"></i>
          ${formatTime(lc.thoiGianBatDau)} - ${formatTime(lc.thoiGianKetThuc)}
          <span style="font-size:0.75rem;color:#b3b3b3">${lc.tenPhong || ''}</span>
          <span class="price-text" style="font-size:0.82rem">${formatMoney(lc.giaVe)}</span>
        </a>`).join('');
    })
    .catch(() => showToast('Không thể tải lịch chiếu', 'error'));
}

/**
 * formatTime: xu ly ca 2 format Jackson tra ve:
 * - Array: [2026, 6, 20, 10, 30]  (LocalDateTime as array)
 * - String: "2026-06-20T10:30:00" (ISO string)
 */
function formatTime(ts) {
  if (!ts) return '--:--';
  let h, m;
  if (Array.isArray(ts)) {
    // Jackson serializes LocalDateTime as [year, month, day, hour, minute, second?]
    h = ts[3] || 0;
    m = ts[4] || 0;
  } else {
    const d = new Date(ts);
    if (isNaN(d)) return '--:--';
    h = d.getHours();
    m = d.getMinutes();
  }
  return String(h).padStart(2,'0') + ':' + String(m).padStart(2,'0');
}

// ---- LOADING OVERLAY ----
function showLoading() { const el = document.querySelector('.loading-overlay'); if (el) el.style.display = 'flex'; }
function hideLoading() { const el = document.querySelector('.loading-overlay'); if (el) el.style.display = 'none'; }

// ---- PROMO SELECT ----
function onPromoChange() {
  const select = document.getElementById('promoSelect');
  const infoEl = document.getElementById('promoInfo');
  const selected = select?.options[select.selectedIndex];
  const totalEl = document.getElementById('summaryTotal');

  if (!selected || !selected.value) {
    if (infoEl) infoEl.innerHTML = '';
    return;
  }

  const loai = selected.dataset.loai;
  const giaTri = parseFloat(selected.dataset.giaTri);
  const baseTotal = parseFloat(document.getElementById('baseTotalAmount')?.value || 0);
  const discount = applyPromo(baseTotal, loai, giaTri);
  const finalTotal = baseTotal - discount;

  if (infoEl) {
    infoEl.innerHTML = `
      <div class="booking-info-row"><span class="booking-info-label">Giảm giá</span><span style="color:#46d369">-${formatMoney(discount)}</span></div>
      <div class="booking-info-row"><span class="booking-info-label">Tổng sau giảm</span><span class="booking-total">${formatMoney(finalTotal)}</span></div>`;
  }
}

// ---- AUTO INIT ----
document.addEventListener('DOMContentLoaded', () => {
  // Show flash messages as toasts
  const success = document.querySelector('[data-toast-success]');
  const error = document.querySelector('[data-toast-error]');
  if (success) showToast(success.dataset.toastSuccess, 'success');
  if (error) showToast(error.dataset.toastError, 'error');

  // Rap selector for schedules
  const rapSelect = document.getElementById('rapSelect');
  if (rapSelect) {
    rapSelect.addEventListener('change', () => {
      // Support both maRap and maRapHidden
      const maRapEl = document.getElementById('maRap') || document.getElementById('maRapHidden');
      if (maRapEl) maRapEl.value = rapSelect.value;
      const activeDate = document.querySelector('.date-pill.active');
      if (activeDate) loadSchedule(activeDate.dataset.ngay);
    });
  }
});
