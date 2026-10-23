import type { Author } from '../api/types'

interface AuthorLabelProps {
  author: Author
}

// 작성자는 닉네임, 기수, 파트로 보여준다
function AuthorLabel({ author }: AuthorLabelProps) {
  return (
    <span className="text-sm text-gray-600">
      {author.nickname} · {author.generation}기 · {author.part}
    </span>
  )
}

export default AuthorLabel
