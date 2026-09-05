ALTER TABLE delivery_requests DROP CONSTRAINT delivery_requests_status_check;
ALTER TABLE delivery_requests ADD CONSTRAINT delivery_requests_status_check CHECK (status IN (
  'CHO_TIEP_NHAN', 'DA_CHAP_NHAN', 'DA_DEN_NHA_HANG', 'DA_LAY_HANG',
  'DANG_VAN_CHUYEN', 'DA_DEN_KHACH_HANG', 'DA_GIAO', 'DA_HUY'
));
-- Existing DA_LAY_HANG rows retain their recorded state; the driver explicitly starts transport.
