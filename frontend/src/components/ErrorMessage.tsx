interface ErrorMessageProps {
  message: string
}

// 서버가 내려준 에러 메시지를 그대로 보여준다. 색만으로 전달하지 않도록 role 도 지정한다
function ErrorMessage({ message }: ErrorMessageProps) {
  return (
    <p role="alert" className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
      {message}
    </p>
  )
}

export default ErrorMessage
