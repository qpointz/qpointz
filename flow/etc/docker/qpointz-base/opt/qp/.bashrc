parse_git_branch() {
   git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/*\ \(.*\)/ [\1]/'
}
export PS1="\[\033[38;5;178m\]\u\[\033[38;5;249m\]:\[\033[32m\]\w\[\033[38;5;27m\]\$(parse_git_branch)\[\033[38;5;249m\] $ "

if [[ -f /opt/qp/profile ]]; then
   source /opt/qp/profile
fi

for f in /opt/qp/usr/[0-9][0-9]_*; do
  source $f
done
