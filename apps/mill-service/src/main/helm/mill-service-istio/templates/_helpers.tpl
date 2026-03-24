{{/*
Expand the name of the chart.
*/}}
{{- define "mill-service-istio.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "mill-service-istio.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "mill-service-istio.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "mill-service-istio.labels" -}}
helm.sh/chart: {{ include "mill-service-istio.chart" . }}
{{ include "mill-service-istio.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- with .Values.commonLabels }}
{{- toYaml . | nindent 0 }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "mill-service-istio.selectorLabels" -}}
app.kubernetes.io/name: {{ include "mill-service-istio.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Gateway name
*/}}
{{- define "mill-service-istio.gatewayName" -}}
{{- if .Values.gateway.name }}
{{- .Values.gateway.name }}
{{- else }}
{{- printf "%s-gateway" (include "mill-service-istio.fullname" .) }}
{{- end }}
{{- end }}

{{/*
VirtualService name
*/}}
{{- define "mill-service-istio.virtualServiceName" -}}
{{- if .Values.virtualService.name }}
{{- .Values.virtualService.name }}
{{- else }}
{{- printf "%s-vs" (include "mill-service-istio.fullname" .) }}
{{- end }}
{{- end }}

{{/*
DestinationRule name
*/}}
{{- define "mill-service-istio.destinationRuleName" -}}
{{- if .Values.destinationRule.name }}
{{- .Values.destinationRule.name }}
{{- else }}
{{- printf "%s-dr" (include "mill-service-istio.fullname" .) }}
{{- end }}
{{- end }}

{{/*
Generate subset name from pod labels or deployment name
*/}}
{{- define "mill-service-istio.subsetName" -}}
{{- if .Values.destination.deploymentName }}
{{- .Values.destination.deploymentName | trunc 63 }}
{{- else if .Values.destination.podLabels }}
{{- $keys := keys .Values.destination.podLabels | sortAlpha }}
{{- $name := "" }}
{{- range $keys }}
  {{- $val := index $.Values.destination.podLabels . }}
  {{- $name = printf "%s-%s" $name $val }}
{{- end }}
{{- trimPrefix "-" $name | trunc 63 }}
{{- else }}
default
{{- end }}
{{- end }}
