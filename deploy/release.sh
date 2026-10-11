#!/usr/bin/env bash
set -euo pipefail
umask 077
cd /opt/bibliouniv
tag=${1:?Missing image tag}
[[ "$tag" =~ ^[0-9a-f]{40}$ ]] || exit 2
exec 9>/opt/bibliouniv/.deploy.lock
flock -n 9 || { echo 'Deployment already running'; exit 1; }
test -s .env || { echo 'Create /opt/bibliouniv/.env first'; exit 1; }
export IMAGE_TAG="$tag"
compose=(docker compose --env-file .env -f "releases/$tag/compose.yml")
"${compose[@]}" config --quiet
if [ -f "releases/$tag/images.tar.gz" ]; then
  docker load -i "releases/$tag/images.tar.gz"
else
  docker image inspect "bibliouniv-api:$tag" "bibliouniv-web:$tag" >/dev/null
fi
# Keep a pre-migration database backup. Restore must be deliberate, not automatic.
mkdir -p backups
if docker inspect bibliouniv-postgres-1 >/dev/null 2>&1; then
  "${compose[@]}" exec -T postgres pg_dump -U saas -d saas -Fc > "backups/before-$tag-$(date -u +%Y%m%dT%H%M%SZ).dump"
fi
"${compose[@]}" up -d --wait --wait-timeout 240
curl --fail --silent --show-error --retry 12 --retry-all-errors --retry-delay 10 \
  https://bibliouniv.duckdns.org/actuator/health
curl --fail --silent --show-error https://bibliouniv.duckdns.org/ -o /dev/null
printf '%s\n' "$tag" > current-release
rm -f "releases/$tag/images.tar.gz"
