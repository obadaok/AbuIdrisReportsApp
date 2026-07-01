#!/bin/bash
# Script to push all historical data to Firebase Realtime Database
# Uses Firebase REST API

BASE_URL="https://bida-ai-999-default-rtdb.firebaseio.com"

echo "Pushing historical report data to Firebase RTDB..."
echo ""

# Function to push report data
push_report() {
    local key=$1
    local month=$2
    local year=$3
    local samples=$4
    local revenue=$5
    local expenses=$6
    local profit=$7
    local expenses_json=$8

    local data=$(cat <<EOF
{
  "monthKey": "$key",
  "monthName": "$month",
  "year": $year,
  "samplesCount": $samples,
  "totalRevenue": $revenue,
  "totalExpenses": $expenses,
  "netProfit": $profit,
  "createdAt": $(date +%s)000,
  "expenseItems": $expenses_json
}
EOF
)
    echo "Pushing $key ($month $year)..."
    curl -s -X PUT "$BASE_URL/reports/$key.json" \
      -H "Content-Type: application/json" \
      -d "$data" > /dev/null
    echo "  -> Done ($samples samples, $revenue revenue)"
}

echo "===== Pushing 16 months of historical data ====="
echo ""

# Feb 2025
push_report "2025-02" "فبراير" 2025 1004 20080000 5382000 14538000 \
'[{"description":"مرتبات","amount":2300000},{"description":"خطاط","amount":50000},{"description":"حديد","amount":212000},{"description":"لافتة","amount":74000},{"description":"الحداد","amount":170000},{"description":"مسامير دربكين + طلاء","amount":30000},{"description":"مصاريف عمال الطاقة الشمسية","amount":71500},{"description":"تركيب مكيف","amount":130000},{"description":"لودر","amount":30000},{"description":"اسمنت + مياه صيانة","amount":115000},{"description":"صحون معمل","amount":45000},{"description":"مشترك","amount":22000},{"description":"أدوات مكتبية","amount":53000},{"description":"أكياس عينات","amount":22000},{"description":"بخور ند","amount":12000},{"description":"كرامة المحل","amount":200000},{"description":"إنترنت","amount":54000},{"description":"طباعة كروت","amount":500000},{"description":"مصروفات","amount":1291500}]'

# Mar 2025
push_report "2025-03" "مارس" 2025 1297 26020000 6645000 19375000 \
'[{"description":"مرتبات","amount":2300000},{"description":"حوافر","amount":2300000},{"description":"مصاريف أخرى","amount":1734000},{"description":"أكياس عينات","amount":24000},{"description":"بخورند","amount":5000},{"description":"إنترنت","amount":62000},{"description":"كرامة العيد","amount":220000}]'

# Apr 2025
push_report "2025-04" "أبريل" 2025 1575 31500000 5459000 26041000 \
'[{"description":"مرتبات","amount":2300000},{"description":"مصروفات عامة","amount":1505000},{"description":"مواصلات","amount":300000},{"description":"تكملة الكروت","amount":200000},{"description":"إنترنت","amount":60000},{"description":"جردل + سلة","amount":15000},{"description":"وصلة ميزان","amount":4000},{"description":"أكياس عينة","amount":24000},{"description":"أقلام","amount":6000},{"description":"حافظة مويه","amount":32000},{"description":"مفتاح در بكين","amount":8000},{"description":"بخورند","amount":5000},{"description":"رسوم المحلية","amount":1000000}]'

# May 2025
push_report "2025-05" "مايو" 2025 2108 41960000 8783000 33177000 \
'[{"description":"مرتبات","amount":2300000},{"description":"ميزان","amount":3500000},{"description":"مصروفات عامة","amount":1536000},{"description":"أكياس عينة","amount":60000},{"description":"بخورند","amount":5000},{"description":"أولاد المحلية","amount":90000},{"description":"شاكوش","amount":20000},{"description":"مواصلات عطبرة (1)","amount":75000},{"description":"مواصلات عطبرة (2)","amount":45000},{"description":"ترحيل ميزان من بورتسودان","amount":150000},{"description":"إنترنت","amount":62000},{"description":"أقلام","amount":30000},{"description":"صحون معمل","amount":5000},{"description":"ورق دق","amount":90000},{"description":"مكنة تغليف","amount":140000},{"description":"شريحة","amount":30000},{"description":"ترحيل كروت","amount":20000},{"description":"أختام","amount":40000},{"description":"كرتونة تغليف","amount":480000},{"description":"طباعة رقم الحساب + ستاندر","amount":25000},{"description":"رسوم هيئة المواصفات","amount":30000}]'

# Jun 2025
push_report "2025-06" "يونيو" 2025 1471 29420000 8313000 21107000 \
'[{"description":"مرتبات","amount":2300000},{"description":"حوافز عيد","amount":3000000},{"description":"إنترنت","amount":100000},{"description":"أكياس عينات","amount":24000},{"description":"مواصلات غزالي","amount":500000},{"description":"بخور ند","amount":5000},{"description":"منصرفات","amount":1464000},{"description":"ضحية","amount":500000},{"description":"متأخرات","amount":420000}]'

# Jul 2025
push_report "2025-07" "يوليو" 2025 1934 38680000 4889000 33791000 \
'[{"description":"مرتبات","amount":2300000},{"description":"بخور ند","amount":12000},{"description":"ترحيل كاميرات","amount":30000},{"description":"منظومة مراقبة + تركيب","amount":724000},{"description":"إنترنت","amount":114000},{"description":"أكياس عينات","amount":36000},{"description":"مصروفات أخرى","amount":1673000}]'

# Aug 2025
push_report "2025-08" "أغسطس" 2025 1886 37720000 8268000 29452000 \
'[{"description":"مرتبات","amount":2900000},{"description":"مصروفات أخرى","amount":1773000},{"description":"ستارلينك + اشتراك","amount":2785000},{"description":"أدوات مكتبية","amount":40000},{"description":"أكياس عينات","amount":40000},{"description":"سيليكون لاصق","amount":15000},{"description":"لودر","amount":50000},{"description":"إنترنت","amount":150000},{"description":"بخور ند","amount":15000},{"description":"مواصلات الكاظم","amount":500000}]'

# Sep 2025
push_report "2025-09" "سبتمبر" 2025 2060 41200000 8875000 32325000 \
'[{"description":"مرتبات","amount":2900000},{"description":"عدسات","amount":515000},{"description":"شهادة خلو نزاع (المحلية)","amount":3100000},{"description":"أكياس عينات","amount":60000},{"description":"شاكوش","amount":20000},{"description":"2 زردية","amount":20000},{"description":"بخور ند","amount":18000},{"description":"شهرية المحلية","amount":70000},{"description":"مصروفات أخرى","amount":2172000}]'

# Oct 2025
push_report "2025-10" "أكتوبر" 2025 2048 102400000 12692000 89708000 \
'[{"description":"مرتبات","amount":7600000},{"description":"كرتونة تغليف","amount":715000},{"description":"عدسات","amount":1030000},{"description":"تنشيط ستارلينك","amount":390000},{"description":"أكياس عينات","amount":50000},{"description":"بخور ند","amount":16000},{"description":"بطانية","amount":150000},{"description":"أولاد المحلية","amount":80000},{"description":"مصروفات أخرى","amount":2661000}]'

# Nov 2025
push_report "2025-11" "نوفمبر" 2025 1988 99400000 12272000 87128000 \
'[{"description":"مرتبات","amount":7600000},{"description":"تنشيط ستارلينك","amount":400000},{"description":"أكياس عينات","amount":50000},{"description":"بخور ند","amount":6000},{"description":"وسائد كراسي","amount":15000},{"description":"طباعة كروت","amount":1600000},{"description":"مصروفات أخرى","amount":2601000}]'

# Dec 2025
push_report "2025-12" "ديسمبر" 2025 2352 117600000 15966000 101634000 \
'[{"description":"مرتبات","amount":7600000},{"description":"ترحيلات متنوعة","amount":86000},{"description":"مواد بناء (زنك، مواسير، خرسانة، سمنت، رملة)","amount":1470000},{"description":"كرتونة وماكينة تغليف","amount":670000},{"description":"تنشيط ستارلينك","amount":400000},{"description":"خلاط وسخان","amount":215000},{"description":"المحلية","amount":300000},{"description":"صيانة كاميرات وهارديسك","amount":240000},{"description":"سيراميك وبوهية","amount":838000},{"description":"مصنعية إجمالية","amount":1000000},{"description":"أخرى (مسامير، أدوات، مياه شغل)","amount":110000},{"description":"مصروفات عامة","amount":3037000}]'

# Jan 2026
push_report "2026-01" "يناير" 2026 2532 126600000 16202000 110398000 \
'[{"description":"مرتبات","amount":7600000},{"description":"تنشيط ستارلينك (5 شهور)","amount":1000000},{"description":"مشوار الصيانة","amount":2900000},{"description":"2 بطارية","amount":860000},{"description":"رخصة تجارية","amount":575000},{"description":"أكياس","amount":90000},{"description":"ترحيل سراميك","amount":180000},{"description":"مصروفات أخرى","amount":2997000}]'

# Feb 2026
push_report "2026-02" "فبراير" 2026 2790 139500000 12980000 126520000 \
'[{"description":"مرتبات","amount":7600000},{"description":"مواصلات الكاظم","amount":500000},{"description":"مصروفات عامة","amount":3208000},{"description":"أكياس عينات","amount":100000},{"description":"ثلاجة","amount":1200000},{"description":"أدوات كهربائية","amount":38000},{"description":"صيانة كاميرات + كاميرا جديدة","amount":320000},{"description":"ملطف جو","amount":14000}]'

# Mar 2026
push_report "2026-03" "مارس" 2026 1180 59000000 40808000 18192000 \
'[{"description":"مرتبات","amount":7600000},{"description":"حوافز","amount":8000000},{"description":"مواصلات عبادة","amount":500000},{"description":"مصروفات عامة","amount":2193000},{"description":"تغليف","amount":4000000},{"description":"ترحيل تغليف","amount":80000},{"description":"ورق عمل","amount":1000000},{"description":"تركيب سراميك","amount":1100000},{"description":"قلاب رمل","amount":50000},{"description":"اسمنت","amount":300000},{"description":"اسمنت ابيض","amount":50000},{"description":"شاشة وحامل","amount":590000},{"description":"وصلات كمبيوتر","amount":30000},{"description":"مشتركات","amount":60000},{"description":"الكهربجي","amount":600000},{"description":"مروحة سقف","amount":175000},{"description":"سندان","amount":40000},{"description":"ترحيل الجهاز الأول","amount":4160000},{"description":"ترحيل الجهاز الثاني","amount":3280000},{"description":"حسن ادريس","amount":7000000}]'

# Apr 2026
push_report "2026-04" "أبريل" 2026 1162 58100000 10160000 47940000 \
'[{"description":"مرتبات","amount":7600000},{"description":"مصروفات عامة","amount":2400000},{"description":"ملطف جو","amount":50000},{"description":"أكياس عينة","amount":30000},{"description":"أقلام","amount":40000},{"description":"موية شغل","amount":40000}]'

# May 2026
push_report "2026-05" "مايو" 2026 2320 116000000 32040000 83960000 \
'[{"description":"حوافز","amount":8000000},{"description":"خلو نزاع","amount":8000000},{"description":"مرتبات","amount":7600000},{"description":"مصروفات عامة","amount":3510000},{"description":"محمد حمد","amount":3000000},{"description":"كرامة العيد","amount":750000},{"description":"مواصلات كاظم","amount":500000},{"description":"رسوم المحلية","amount":500000},{"description":"أكياس عينة","amount":72000},{"description":"ملطف جو","amount":58000},{"description":"نظافة مكيف","amount":50000}]'

echo ""
echo "===== All 16 reports pushed successfully ====="
echo "You can verify at: $BASE_URL/reports.json"
echo ""

# Push a notification to trigger the app to sync
NOTIF_DATA='{"type":"general","title":"تم تحديث البيانات","body":"جميع التقارير القديمة متاحة الآن في التطبيق","timestamp":'$(date +%s)'000,"sent":false}'
echo "Sending notification to trigger sync..."
curl -s -X POST "$BASE_URL/notifications.json" \
  -H "Content-Type: application/json" \
  -d "$NOTIF_DATA" > /dev/null
echo "Notification sent."
echo "Done!"
