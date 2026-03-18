$ErrorActionPreference = "Stop"

$repoRoot = "C:\Users\vm\wip\qpointz\qpointz"
$outDir = Join-Path $repoRoot "docs\design\platform\artifacts"
$tmpDir = Join-Path $env:TEMP ("mill-onepager-ppt-" + [guid]::NewGuid().ToString("N"))

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null

$pngPath = Join-Path $outDir "mill-data-lane-onepager.png"
$pptxPath = Join-Path $outDir "mill-data-lane-onepager.pptx"
$zipPath = Join-Path $outDir "mill-data-lane-onepager.zip"
$staging = Join-Path $tmpDir "pptx"

New-Item -ItemType Directory -Force -Path $staging | Out-Null

$py = @'
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

W, H = 1600, 900
bg = "#F3F1EA"
ink = "#13202B"
muted = "#5D6B75"
navy = "#15364A"
teal = "#2D8C84"
orange = "#D47A2A"
cream = "#FFF9F0"
line = "#AAB8BF"
light = "#E3E8E3"

out = Path(r"__PNG_PATH__")
img = Image.new("RGB", (W, H), bg)
d = ImageDraw.Draw(img)

def font(size, bold=False):
    candidates = []
    if bold:
        candidates += [
            r"C:\Windows\Fonts\arialbd.ttf",
            r"C:\Windows\Fonts\segoeuib.ttf",
            r"C:\Windows\Fonts\calibrib.ttf",
        ]
    else:
        candidates += [
            r"C:\Windows\Fonts\arial.ttf",
            r"C:\Windows\Fonts\segoeui.ttf",
            r"C:\Windows\Fonts\calibri.ttf",
        ]
    for c in candidates:
        try:
            return ImageFont.truetype(c, size)
        except OSError:
            pass
    return ImageFont.load_default()

title_f = font(38, True)
subtitle_f = font(18, False)
section_f = font(18, True)
body_f = font(15, False)
small_f = font(13, False)

def rr(xy, fill, outline=None, radius=26, width=2):
    d.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)

def txt(x, y, text, f, fill=ink, anchor=None, spacing=4):
    d.multiline_text((x, y), text, font=f, fill=fill, anchor=anchor, spacing=spacing)

def arrow(x1, y1, x2, y2, fill=navy, width=4):
    d.line((x1, y1, x2, y2), fill=fill, width=width)
    import math
    ang = math.atan2(y2 - y1, x2 - x1)
    ah = 12
    a1 = ang + math.pi * 0.85
    a2 = ang - math.pi * 0.85
    p1 = (x2 + ah * math.cos(a1), y2 + ah * math.sin(a1))
    p2 = (x2 + ah * math.cos(a2), y2 + ah * math.sin(a2))
    d.polygon([(x2, y2), p1, p2], fill=fill)

def wrap_lines(items, bullet="• "):
    return "\n".join(f"{bullet}{x}" for x in items)

# Header
d.rectangle((0, 0, W, 96), fill=navy)
txt(54, 22, "Mill Architecture", title_f, fill=cream)
txt(56, 64, "One-page data lane view: heterogeneous data -> SQL + metadata service -> multi-client consumption", subtitle_f, fill="#D6E5EA")

# Column frames
left = (42, 128, 340, 820)
mid = (382, 128, 1188, 820)
right = (1230, 128, 1558, 820)

# Left column
rr(left, fill=cream, outline=line, radius=30)
txt(64, 148, "1. Sources & Use Cases", section_f, fill=navy)

rr((64, 188, 318, 360), fill="#F7FBFC", outline="#B5C7CF", radius=22)
txt(82, 206, "Data sources", section_f, fill=teal)
txt(82, 236, wrap_lines([
    "Files: CSV, TSV, Parquet, Avro, Excel",
    "Databases via JDBC",
    "Mixed-format domains like skymill",
]), body_f, fill=ink)

rr((64, 386, 318, 620), fill="#FFF5E8", outline="#D9B08A", radius=22)
txt(82, 404, "Representative use cases", section_f, fill=orange)
txt(82, 434, wrap_lines([
    "Quick Python query against running service",
    "Expose local CSVs through Flow backend",
    "Front a relational DB through JDBC backend",
    "Join CSV + Parquet + Excel in one schema",
    "Consume rows, Arrow, pandas, polars, JDBC",
]), body_f, fill=ink)

rr((64, 646, 318, 792), fill="#F5F8EF", outline="#B6C6A3", radius=22)
txt(82, 664, "Key idea", section_f, fill="#5C7A36")
txt(82, 698, "Mill separates client contract from backend/storage shape.\nClients see one SQL + metadata surface even when data is heterogeneous.", body_f, fill=ink)

# Center canvas
rr(mid, fill="#FBFCF9", outline=line, radius=30)
txt(410, 148, "2. Core Data Lane", section_f, fill=navy)

# Top service band
rr((410, 182, 1160, 262), fill="#E8F1F4", outline="#B7C7D0", radius=22)
txt(432, 200, "Service surface", section_f, fill=navy)
txt(432, 228, "Handshake   |   ListSchemas / GetSchema   |   GetDialect   |   ParseSql / ExecQuery / SubmitQuery / FetchQueryResult", body_f, fill=ink)

# Endpoint boxes
rr((430, 294, 632, 374), fill="#EEF7F6", outline="#A3CDC7", radius=18)
rr((938, 294, 1140, 374), fill="#EEF7F6", outline="#A3CDC7", radius=18)
txt(531, 318, "gRPC", section_f, fill=teal, anchor="mm")
txt(531, 346, "streaming query path", small_f, fill=muted, anchor="mm")
txt(1039, 318, "HTTP", section_f, fill=teal, anchor="mm")
txt(1039, 346, "metadata + paged results", small_f, fill=muted, anchor="mm")

# Dispatcher
rr((652, 292, 918, 382), fill="#15364A", outline="#15364A", radius=20)
txt(785, 320, "DataOperationDispatcher", section_f, fill=cream, anchor="mm")
txt(785, 348, "single contract boundary", small_f, fill="#D7E4EA", anchor="mm")

# Engine and metadata lanes
rr((430, 438, 760, 632), fill="#FFF6EA", outline="#D9B08A", radius=24)
txt(456, 462, "SQL / execution lane", section_f, fill=orange)
txt(456, 496, wrap_lines([
    "SqlProvider parses and plans SQL",
    "Apache Calcite-based planning layer",
    "Plan rewrite / security / paging",
    "ExecutionProvider runs against selected backend",
    "Results returned as columnar VectorBlock chunks",
]), body_f, fill=ink)

rr((826, 438, 1140, 632), fill="#EEF7F6", outline="#A3CDC7", radius=24)
txt(852, 462, "Metadata lane", section_f, fill=teal)
txt(852, 496, wrap_lines([
    "Physical schema metadata",
    "Descriptive / relation / value-mapping facets",
    "Dialect descriptor and SQL capability metadata",
    "Shared by clients, UI, and AI flows",
]), body_f, fill=ink)

# Backend band
rr((430, 684, 1140, 792), fill="#F1F4EE", outline="#B7C1B6", radius=24)
txt(454, 706, "Backends", section_f, fill="#5C7A36")

backend_boxes = [
    (454, 738, 654, 774, "Flow", "files + mixed formats"),
    (684, 738, 884, 774, "Calcite", "model / federation"),
    (914, 738, 1114, 774, "JDBC", "relational systems"),
]
for x1, y1, x2, y2, t1, t2 in backend_boxes:
    rr((x1, y1, x2, y2), fill=cream, outline="#C7D0C6", radius=16)
    txt((x1+x2)//2, y1+10, t1, section_f, fill=navy, anchor="ma")
    txt((x1+x2)//2, y1+34, t2, small_f, fill=muted, anchor="ma")

# Arrows
arrow(531, 374, 720, 320)
arrow(1039, 374, 850, 320)
arrow(785, 382, 785, 430)
arrow(595, 632, 595, 684)
arrow(983, 632, 983, 684)

# Result ribbon
rr((520, 396, 1050, 424), fill=teal, outline=teal, radius=14)
txt(785, 410, "Unified service contract -> vectorized results + metadata", small_f, fill=cream, anchor="mm")

# Right column
rr(right, fill=cream, outline=line, radius=30)
txt(1252, 148, "3. Client Consumption", section_f, fill=navy)

client_specs = [
    ("Python", "query rows, Arrow, pandas, polars", "#EEF7F6", "#A3CDC7", teal),
    ("JDBC", "Java apps, BI tools, SQL consumers", "#FFF6EA", "#D9B08A", orange),
    ("UI", "chat, schema exploration, HTTP apps", "#F7FBFC", "#B5C7CF", navy),
    ("AI", "NL2SQL, reasoning, metadata-aware flows", "#F5F8EF", "#B6C6A3", "#5C7A36"),
]
top = 190
for title, desc, fillc, ol, accent in client_specs:
    rr((1252, top, 1534, top+112), fill=fillc, outline=ol, radius=22)
    txt(1272, top+18, title, section_f, fill=accent)
    txt(1272, top+50, desc, body_f, fill=ink)
    top += 136

rr((1252, 742, 1534, 792), fill=navy, outline=navy, radius=18)
txt(1393, 758, "One logical SQL + metadata surface", small_f, fill=cream, anchor="mm")
txt(1393, 778, "across files, DBs, and mixed sources", small_f, fill="#D7E4EA", anchor="mm")

# Footer
txt(52, 850, "Examples reflected: QuickStart, ConnectCSV, JdbcBackend, Dataframes, MixingFormats", small_f, fill=muted)
txt(1548, 850, "docs.qpointz.io + internal design docs", small_f, fill=muted, anchor="ra")

img.save(out)
print(out)
'@

$py = $py.Replace("__PNG_PATH__", $pngPath.Replace("\", "\\"))

@"
$py
"@ | python -

function Write-Utf8NoBom {
    param(
        [string]$Path,
        [string]$Content
    )
    $dir = Split-Path -Parent $Path
    if ($dir) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    $enc = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $enc)
}

New-Item -ItemType Directory -Force -Path (Join-Path $staging "_rels") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "docProps") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\_rels") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\slides\_rels") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\slides") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\media") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\slideLayouts\_rels") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\slideLayouts") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\slideMasters\_rels") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\slideMasters") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $staging "ppt\theme") | Out-Null

Copy-Item $pngPath (Join-Path $staging "ppt\media\image1.png") -Force

$coreDate = (Get-Date).ToUniversalTime().ToString("s") + "Z"

Write-Utf8NoBom (Join-Path $staging "[Content_Types].xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
  <Override PartName="/ppt/slides/slide1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
  <Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
  <Override PartName="/ppt/presProps.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presProps+xml"/>
  <Override PartName="/ppt/viewProps.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.viewProps+xml"/>
  <Override PartName="/ppt/tableStyles.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml"/>
</Types>
'@

Write-Utf8NoBom (Join-Path $staging "_rels\.rels") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>
'@

Write-Utf8NoBom (Join-Path $staging "docProps\app.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Codex</Application>
  <Slides>1</Slides>
  <Notes>0</Notes>
  <HiddenSlides>0</HiddenSlides>
  <MMClips>0</MMClips>
  <ScaleCrop>false</ScaleCrop>
  <HeadingPairs>
    <vt:vector size="2" baseType="variant">
      <vt:variant><vt:lpstr>Theme</vt:lpstr></vt:variant>
      <vt:variant><vt:i4>1</vt:i4></vt:variant>
    </vt:vector>
  </HeadingPairs>
  <TitlesOfParts>
    <vt:vector size="1" baseType="lpstr">
      <vt:lpstr>Mill Architecture</vt:lpstr>
    </vt:vector>
  </TitlesOfParts>
  <Company>QPointz</Company>
  <LinksUpToDate>false</LinksUpToDate>
  <SharedDoc>false</SharedDoc>
  <HyperlinksChanged>false</HyperlinksChanged>
  <AppVersion>16.0000</AppVersion>
</Properties>
'@

Write-Utf8NoBom (Join-Path $staging "docProps\core.xml") @"
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>Mill Architecture One-Pager</dc:title>
  <dc:subject>Mill data lane architecture</dc:subject>
  <dc:creator>Codex</dc:creator>
  <cp:lastModifiedBy>Codex</cp:lastModifiedBy>
  <dcterms:created xsi:type="dcterms:W3CDTF">$coreDate</dcterms:created>
  <dcterms:modified xsi:type="dcterms:W3CDTF">$coreDate</dcterms:modified>
</cp:coreProperties>
"@

Write-Utf8NoBom (Join-Path $staging "ppt\presentation.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" saveSubsetFonts="1" autoCompressPictures="0">
  <p:sldMasterIdLst>
    <p:sldMasterId id="2147483648" r:id="rId1"/>
  </p:sldMasterIdLst>
  <p:sldIdLst>
    <p:sldId id="256" r:id="rId7"/>
  </p:sldIdLst>
  <p:sldSz cx="12192000" cy="6858000" type="screen16x9"/>
  <p:notesSz cx="6858000" cy="9144000"/>
  <p:defaultTextStyle/>
</p:presentation>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\_rels\presentation.xml.rels") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/presProps" Target="presProps.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/viewProps" Target="viewProps.xml"/>
  <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="theme/theme1.xml"/>
  <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableStyles" Target="tableStyles.xml"/>
  <Relationship Id="rId7" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide1.xml"/>
</Relationships>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\presProps.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentationPr xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"/>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\viewProps.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:viewPr xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:normalViewPr>
    <p:restoredLeft sz="15620"/>
    <p:restoredTop sz="94660"/>
  </p:normalViewPr>
  <p:slideViewPr>
    <p:cSldViewPr snapToGrid="1" snapToObjects="1"/>
  </p:slideViewPr>
  <p:notesTextViewPr/>
  <p:gridSpacing cx="72008" cy="72008"/>
</p:viewPr>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\tableStyles.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:tblStyleLst xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" def="{5C22544A-7EE6-4342-B048-85BDC9FD1C3A}"/>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\theme\theme1.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="Office Theme">
  <a:themeElements>
    <a:clrScheme name="Office">
      <a:dk1><a:srgbClr val="000000"/></a:dk1>
      <a:lt1><a:srgbClr val="FFFFFF"/></a:lt1>
      <a:dk2><a:srgbClr val="1F497D"/></a:dk2>
      <a:lt2><a:srgbClr val="EEECE1"/></a:lt2>
      <a:accent1><a:srgbClr val="4F81BD"/></a:accent1>
      <a:accent2><a:srgbClr val="C0504D"/></a:accent2>
      <a:accent3><a:srgbClr val="9BBB59"/></a:accent3>
      <a:accent4><a:srgbClr val="8064A2"/></a:accent4>
      <a:accent5><a:srgbClr val="4BACC6"/></a:accent5>
      <a:accent6><a:srgbClr val="F79646"/></a:accent6>
      <a:hlink><a:srgbClr val="0000FF"/></a:hlink>
      <a:folHlink><a:srgbClr val="800080"/></a:folHlink>
    </a:clrScheme>
    <a:fontScheme name="Office">
      <a:majorFont>
        <a:latin typeface="Arial"/>
        <a:ea typeface=""/>
        <a:cs typeface=""/>
      </a:majorFont>
      <a:minorFont>
        <a:latin typeface="Arial"/>
        <a:ea typeface=""/>
        <a:cs typeface=""/>
      </a:minorFont>
    </a:fontScheme>
    <a:fmtScheme name="Office">
      <a:fillStyleLst>
        <a:solidFill><a:schemeClr val="phClr"/></a:solidFill>
        <a:gradFill rotWithShape="1"><a:gsLst><a:gs pos="0"><a:schemeClr val="phClr"/></a:gs><a:gs pos="100000"><a:schemeClr val="phClr"/></a:gs></a:gsLst><a:lin ang="5400000" scaled="0"/></a:gradFill>
        <a:gradFill rotWithShape="1"><a:gsLst><a:gs pos="0"><a:schemeClr val="phClr"/></a:gs><a:gs pos="100000"><a:schemeClr val="phClr"/></a:gs></a:gsLst><a:lin ang="5400000" scaled="0"/></a:gradFill>
      </a:fillStyleLst>
      <a:lnStyleLst>
        <a:ln w="9525" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln>
        <a:ln w="25400" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln>
        <a:ln w="38100" cap="flat" cmpd="sng" algn="ctr"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln>
      </a:lnStyleLst>
      <a:effectStyleLst>
        <a:effectStyle><a:effectLst/></a:effectStyle>
        <a:effectStyle><a:effectLst/></a:effectStyle>
        <a:effectStyle><a:effectLst/></a:effectStyle>
      </a:effectStyleLst>
      <a:bgFillStyleLst>
        <a:solidFill><a:schemeClr val="phClr"/></a:solidFill>
        <a:gradFill rotWithShape="1"><a:gsLst><a:gs pos="0"><a:schemeClr val="phClr"/></a:gs><a:gs pos="100000"><a:schemeClr val="phClr"/></a:gs></a:gsLst><a:lin ang="5400000" scaled="0"/></a:gradFill>
        <a:gradFill rotWithShape="1"><a:gsLst><a:gs pos="0"><a:schemeClr val="phClr"/></a:gs><a:gs pos="100000"><a:schemeClr val="phClr"/></a:gs></a:gsLst><a:lin ang="5400000" scaled="0"/></a:gradFill>
      </a:bgFillStyleLst>
    </a:fmtScheme>
  </a:themeElements>
  <a:objectDefaults/>
  <a:extraClrSchemeLst/>
</a:theme>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\slideMasters\slideMaster1.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld name="Blank Master">
    <p:bg>
      <p:bgPr>
        <a:solidFill><a:srgbClr val="FFFFFF"/></a:solidFill>
      </p:bgPr>
    </p:bg>
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMap accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" bg1="lt1" bg2="lt2" folHlink="folHlink" hlink="hlink" tx1="dk1" tx2="dk2"/>
  <p:sldLayoutIdLst>
    <p:sldLayoutId id="1" r:id="rId1"/>
  </p:sldLayoutIdLst>
  <p:txStyles>
    <p:titleStyle/>
    <p:bodyStyle/>
    <p:otherStyle/>
  </p:txStyles>
</p:sldMaster>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\slideMasters\_rels\slideMaster1.xml.rels") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>
</Relationships>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\slideLayouts\slideLayout1.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" type="blank" preserve="1">
  <p:cSld name="Blank">
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sldLayout>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\slideLayouts\_rels\slideLayout1.xml.rels") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\slides\slide1.xml") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld>
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
      <p:pic>
        <p:nvPicPr>
          <p:cNvPr id="2" name="Mill Architecture Poster"/>
          <p:cNvPicPr>
            <a:picLocks noChangeAspect="1"/>
          </p:cNvPicPr>
          <p:nvPr/>
        </p:nvPicPr>
        <p:blipFill>
          <a:blip r:embed="rId2"/>
          <a:stretch><a:fillRect/></a:stretch>
        </p:blipFill>
        <p:spPr>
          <a:xfrm>
            <a:off x="0" y="0"/>
            <a:ext cx="12192000" cy="6858000"/>
          </a:xfrm>
          <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
        </p:spPr>
      </p:pic>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>
'@

Write-Utf8NoBom (Join-Path $staging "ppt\slides\_rels\slide1.xml.rels") @'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/image1.png"/>
</Relationships>
'@

if (Test-Path $pptxPath) {
    Remove-Item $pptxPath -Force
}
if (Test-Path $zipPath) {
    Remove-Item $zipPath -Force
}

Compress-Archive -Path (Join-Path $staging "*") -DestinationPath $zipPath
Move-Item $zipPath $pptxPath

Write-Output "PNG=$pngPath"
Write-Output "PPTX=$pptxPath"
