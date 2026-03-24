{{/*
Expand the name of the chart.
*/}}
{{- define "mill-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "mill-service.fullname" -}}
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
{{- define "mill-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "mill-service.labels" -}}
helm.sh/chart: {{ include "mill-service.chart" . }}
{{ include "mill-service.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "mill-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "mill-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "mill-service.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "mill-service.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Return the secret name to use
*/}}
{{- define "mill-service.secretName" -}}
{{- if .Values.secrets.create }}
{{- include "mill-service.fullname" . }}
{{- else }}
{{- .Values.secrets.existingSecretName }}
{{- end }}
{{- end }}

{{/*
Return the configmap name
*/}}
{{- define "mill-service.configMapName" -}}
{{- include "mill-service.fullname" . }}-config
{{- end }}
