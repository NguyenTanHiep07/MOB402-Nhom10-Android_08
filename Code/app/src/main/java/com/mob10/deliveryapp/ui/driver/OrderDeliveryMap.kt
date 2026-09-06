package com.mob10.deliveryapp.ui.driver

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mob10.deliveryapp.ui.theme.UthError
import com.mob10.deliveryapp.ui.theme.UthOutlineVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSuccess
import com.mob10.deliveryapp.ui.theme.UthWarning
import com.mob10.deliveryapp.ui.theme.UthWarningContainer
import com.mob10.deliveryapp.util.GeoPoint
import com.mob10.deliveryapp.util.LocationHelper

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OrderDeliveryMap(
    pickupPoint: GeoPoint?,
    deliveryPoint: GeoPoint?,
    driverPoint: GeoPoint?,
    pickupTitle: String = "Điểm lấy hàng",
    deliveryTitle: String = "Điểm giao hàng",
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectivePickup = pickupPoint ?: GeoPoint(10.7769, 106.7009)
    val effectiveDelivery = deliveryPoint ?: GeoPoint(10.7827, 106.6959)
    val effectiveDriver = driverPoint ?: LocationHelper.DEFAULT_HCM_LOCATION

    val mapHtml = remember(effectivePickup, effectiveDelivery, driverPoint, hasLocationPermission) {
        buildMapHtml(
            pickup = effectivePickup,
            delivery = effectiveDelivery,
            driver = if (hasLocationPermission) effectiveDriver else null,
            pickupTitle = pickupTitle,
            deliveryTitle = deliveryTitle
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, UthOutlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Interactive Map via WebView
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        webViewClient = WebViewClient()
                        loadDataWithBaseURL("https://maps.google.com", mapHtml, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL("https://maps.google.com", mapHtml, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Banner trạng thái quyền vị trí
            if (!hasLocationPermission) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onRequestPermission() }
                        .align(Alignment.TopCenter),
                    color = UthWarningContainer.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = UthWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chưa bật GPS tài xế. Nhấn để cấp quyền vị trí",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = UthPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GPS Đã kết nối",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = UthPrimary
                        )
                    }
                }
            }

            // 3. Legend chú thích Marker ở góc dưới bản đồ
            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomStart),
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(UthPrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Điểm lấy", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(UthError))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Điểm giao", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

                    if (hasLocationPermission) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(UthSuccess))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tài xế", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

private fun buildMapHtml(
    pickup: GeoPoint,
    delivery: GeoPoint,
    driver: GeoPoint?,
    pickupTitle: String,
    deliveryTitle: String
): String {
    val driverMarkerJs = if (driver != null) {
        """
        var driverIcon = L.divIcon({
            html: '<div style="background:#10B981;color:white;border-radius:50%;width:26px;height:26px;display:flex;align-items:center;justify-content:center;font-size:14px;box-shadow:0 2px 6px rgba(0,0,0,0.3);border:2px solid white;">🛵</div>',
            className: '',
            iconSize: [26, 26],
            iconAnchor: [13, 13]
        });
        var driverMarker = L.marker([${driver.latitude}, ${driver.longitude}], {icon: driverIcon})
            .addTo(map)
            .bindPopup('<b>Vị trí tài xế</b><br>Đang di chuyển');
        points.push([${driver.latitude}, ${driver.longitude}]);
        """
    } else ""

    val polylineJs = if (driver != null) {
        "var routeLine = L.polyline([[${driver.latitude}, ${driver.longitude}], [${pickup.latitude}, ${pickup.longitude}], [${delivery.latitude}, ${delivery.longitude}]], {color: '#0066FF', weight: 5, opacity: 0.85, dashArray: '8, 6'}).addTo(map);"
    } else {
        "var routeLine = L.polyline([[${pickup.latitude}, ${pickup.longitude}], [${delivery.latitude}, ${delivery.longitude}]], {color: '#0066FF', weight: 5, opacity: 0.85}).addTo(map);"
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            html, body, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: #F3F4F6; }
            .leaflet-control-attribution { display: none !important; }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            var map = L.map('map', { zoomControl: false }).setView([${pickup.latitude}, ${pickup.longitude}], 14);
            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19
            }).addTo(map);

            var points = [];

            // Marker Điểm lấy hàng (Kho/Cửa hàng)
            var pickupIcon = L.divIcon({
                html: '<div style="background:#0066FF;color:white;border-radius:50%;width:26px;height:26px;display:flex;align-items:center;justify-content:center;font-size:14px;box-shadow:0 2px 6px rgba(0,0,0,0.3);border:2px solid white;">🏬</div>',
                className: '',
                iconSize: [26, 26],
                iconAnchor: [13, 13]
            });
            var pickupMarker = L.marker([${pickup.latitude}, ${pickup.longitude}], {icon: pickupIcon})
                .addTo(map)
                .bindPopup('<b>Điểm lấy hàng</b><br>${pickupTitle.replace("'", "\\'")}');
            points.push([${pickup.latitude}, ${pickup.longitude}]);

            // Marker Điểm giao hàng (Khách nhận)
            var deliveryIcon = L.divIcon({
                html: '<div style="background:#EF4444;color:white;border-radius:50%;width:26px;height:26px;display:flex;align-items:center;justify-content:center;font-size:14px;box-shadow:0 2px 6px rgba(0,0,0,0.3);border:2px solid white;">🏁</div>',
                className: '',
                iconSize: [26, 26],
                iconAnchor: [13, 13]
            });
            var deliveryMarker = L.marker([${delivery.latitude}, ${delivery.longitude}], {icon: deliveryIcon})
                .addTo(map)
                .bindPopup('<b>Điểm giao hàng</b><br>${deliveryTitle.replace("'", "\\'")}');
            points.push([${delivery.latitude}, ${delivery.longitude}]);

            $driverMarkerJs

            $polylineJs

            if (points.length > 1) {
                map.fitBounds(points, { padding: [40, 40] });
            }
        </script>
    </body>
    </html>
    """.trimIndent()
}
