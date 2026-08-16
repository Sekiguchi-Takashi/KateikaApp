#!/data/data/com.termux/files/usr/bin/bash
cd "$(dirname "$0")"
TOKEN=$(git config --global github.token)
GHUSER=Sekiguchi-Takashi
REPO=KateikaApp
API=https://api.github.com
curl -s -o /dev/null -X POST -H "Authorization: token $TOKEN" -d "{\"name\":\"$REPO\"}" $API/user/repos
if [ ! -d .git ]; then git init -b main; fi
git remote remove origin 2>/dev/null
git remote add origin https://$GHUSER:$TOKEN@github.com/$GHUSER/$REPO.git
git add -A
git commit -m "${1:-update}"
git pull --rebase origin main
git push -u origin main
LATEST=$(git ls-remote --tags origin | grep -o 'refs/tags/v[0-9.]*$' | sed 's|refs/tags/||' | sort -V | tail -n1)
if [ -z "$LATEST" ]; then NEXT=v1.0.0; else BASE=${LATEST%.*}; PATCH=${LATEST##*.}; NEXT=$BASE.$((PATCH+1)); fi
SHA=$(git rev-parse HEAD)
curl -s -o /dev/null -X POST -H "Authorization: token $TOKEN" -d "{\"ref\":\"refs/tags/$NEXT\",\"sha\":\"$SHA\"}" $API/repos/$GHUSER/$REPO/git/refs
printf 'pushed and tagged %s\n' "$NEXT"
