import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import {
  Check,
  PencilSimple,
  Trash,
  X,
  ArrowBendDownRight,
  LockKey,
} from '@phosphor-icons/react'
import { api } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { Avatar } from '../ui/Avatar'
import { Button } from '../ui/Button'
import { Textarea } from '../ui/Field'
import { Skeleton } from '../ui/Spinner'
import { formatDateTime, timeAgo } from '../../lib/format'
import { cn } from '../../lib/cn'

function buildTree(comments) {
  const byParent = new Map()
  comments.forEach((comment) => {
    const key = comment.parentCommentId || 'root'
    if (!byParent.has(key)) byParent.set(key, [])
    byParent.get(key).push(comment)
  })
  return byParent
}

function CommentItem({ comment, byParent, depth, fileId, onReply }) {
  const { profile, isMaster, isAuthenticated } = useAuth()
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [content, setContent] = useState(comment.content)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const replies = byParent.get(comment.id) || []
  const own = profile?.username && profile.username === comment.authorUsername

  const editMutation = useMutation({
    mutationFn: () => api.forum.edit(fileId, comment.id, content.trim()),
    onSuccess: () => {
      toast.success('Comentario actualizado')
      setEditing(false)
      queryClient.invalidateQueries({ queryKey: ['comments', String(fileId)] })
    },
    onError: (error) => toast.error(error.message),
  })

  const deleteMutation = useMutation({
    mutationFn: () => api.forum.remove(fileId, comment.id),
    onSuccess: () => {
      toast.success('Comentario eliminado')
      queryClient.invalidateQueries({ queryKey: ['comments', String(fileId)] })
    },
    onError: (error) => toast.error(error.message),
  })

  return (
    <div className={cn(depth > 0 && 'ml-8 border-l border-ink/8 pl-4 sm:ml-12 sm:pl-6')}>
      <article className="group py-5">
        <div className="flex items-start gap-3">
          <Avatar name={comment.authorUsername} size="sm" />
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
              <p className="text-sm font-semibold text-ink">
                {comment.authorUsername || 'Anónimo'}
              </p>
              {own && <span className="chip py-0.5 text-[0.55rem]">Tú</span>}
              <span className="text-[0.65rem] text-ink-faint" title={formatDateTime(comment.createdAt)}>
                {timeAgo(comment.createdAt)}
              </span>
              {comment.editedAt && (
                <span className="text-[0.6rem] text-ink-faint">· editado</span>
              )}
            </div>

            {editing ? (
              <div className="mt-3">
                <Textarea
                  value={content}
                  onChange={(event) => setContent(event.target.value)}
                  maxLength={2000}
                  aria-label="Editar comentario"
                />
                <div className="mt-2 flex gap-2">
                  <Button
                    onClick={() => editMutation.mutate()}
                    loading={editMutation.isPending}
                    className="min-h-8 px-3 py-1.5 text-[0.7rem]"
                    icon={<Check size={13} weight="bold" />}
                  >
                    Guardar
                  </Button>
                  <Button
                    variant="ghost"
                    onClick={() => {
                      setContent(comment.content)
                      setEditing(false)
                    }}
                    className="min-h-8 px-3 py-1.5 text-[0.7rem]"
                  >
                    Cancelar
                  </Button>
                </div>
              </div>
            ) : (
              <p className="mt-2 whitespace-pre-wrap text-[0.88rem] leading-7 text-ink-soft">
                {comment.content}
              </p>
            )}

            {comment.referencedFile && (
              <Link
                to={comment.referencedFile.available ? `/material/${comment.referencedFile.id}` : '/explorar'}
                className="mt-3 inline-flex max-w-full items-center gap-2 rounded-xl border border-ink/8 bg-white/45 px-3 py-2 text-xs text-ink-soft transition-colors hover:bg-white/75"
              >
                <span className="size-1.5 shrink-0 rounded-full bg-aqua-500" />
                <span className="truncate">
                  {comment.referencedFile.title || 'Material referenciado'}
                </span>
                <span className="shrink-0 text-ink-faint">
                  {comment.referencedFile.subject || ''}
                </span>
              </Link>
            )}

            {!editing && (
              <div className="mt-2 flex items-center gap-3">
                {isAuthenticated && depth < 2 && (
                  <button
                    type="button"
                    onClick={() => onReply(comment.id)}
                    className="inline-flex items-center gap-1 text-[0.65rem] font-semibold text-ink-faint transition-colors hover:text-aqua-800"
                  >
                    <ArrowBendDownRight size={13} aria-hidden="true" /> Responder
                  </button>
                )}
                {own && (
                  <button
                    type="button"
                    onClick={() => setEditing(true)}
                    className="inline-flex items-center gap-1 text-[0.65rem] font-semibold text-ink-faint transition-colors hover:text-ink"
                  >
                    <PencilSimple size={12} aria-hidden="true" /> Editar
                  </button>
                )}
                {(own || isMaster) && (
                  <button
                    type="button"
                    onClick={() => {
                      if (!confirmDelete) {
                        setConfirmDelete(true)
                        window.setTimeout(() => setConfirmDelete(false), 3500)
                        return
                      }
                      deleteMutation.mutate()
                    }}
                    disabled={deleteMutation.isPending}
                    className={cn(
                      'inline-flex items-center gap-1 text-[0.65rem] font-semibold transition-colors',
                      confirmDelete
                        ? 'text-[#8b4b43]'
                        : 'text-ink-faint hover:text-[#934b43]',
                    )}
                  >
                    <Trash size={12} aria-hidden="true" />
                    {confirmDelete ? '¿Seguro?' : 'Eliminar'}
                  </button>
                )}
              </div>
            )}
          </div>
        </div>
      </article>

      {replies.map((reply) => (
        <CommentItem
          key={reply.id}
          comment={reply}
          byParent={byParent}
          depth={depth + 1}
          fileId={fileId}
          onReply={onReply}
        />
      ))}
    </div>
  )
}

export function Thread({ fileId }) {
  const { isAuthenticated } = useAuth()
  const queryClient = useQueryClient()
  const [content, setContent] = useState('')
  const [parentId, setParentId] = useState(null)
  const commentsQuery = useQuery({
    queryKey: ['comments', String(fileId)],
    queryFn: () => api.forum.list(fileId, 0, 100),
    enabled: Boolean(fileId),
  })

  const comments = useMemo(() => commentsQuery.data || [], [commentsQuery.data])
  const byParent = useMemo(() => buildTree(comments), [comments])

  const createMutation = useMutation({
    mutationFn: () =>
      api.forum.create(fileId, {
        content: content.trim(),
        ...(parentId ? { parentCommentId: parentId } : {}),
      }),
    onSuccess: () => {
      setContent('')
      setParentId(null)
      toast.success(parentId ? 'Respuesta publicada' : 'Comentario publicado')
      queryClient.invalidateQueries({ queryKey: ['comments', String(fileId)] })
    },
    onError: (error) => toast.error(error.message),
  })

  const submit = (event) => {
    event.preventDefault()
    if (!content.trim()) return
    createMutation.mutate()
  }

  const roots = byParent.get('root') || []

  return (
    <section id="hilo" className="scroll-mt-28">
      <div className="flex items-center justify-between gap-4 border-b border-ink/8 pb-5">
        <div>
          <p className="eyebrow text-aqua-700">Conversación</p>
          <h2 className="mt-2 flex items-center gap-2 text-2xl font-semibold tracking-[-0.03em] text-ink">
            Hilo del material
            <span className="text-base font-normal text-ink-faint">
              {comments.length || ''}
            </span>
          </h2>
        </div>
        <span className="hidden text-xs text-ink-faint sm:block">
          {comments.length} {comments.length === 1 ? 'comentario' : 'comentarios'}
        </span>
      </div>

      {isAuthenticated ? (
        <form onSubmit={submit} className="mt-6">
          {parentId && (
            <div className="mb-2 flex items-center justify-between gap-3 rounded-xl bg-aqua-50 px-3 py-2 text-[0.68rem] text-aqua-800">
              <span>Respondiendo a un comentario</span>
              <button
                type="button"
                onClick={() => setParentId(null)}
                className="inline-flex items-center gap-1 font-semibold hover:text-aqua-900"
              >
                <X size={12} weight="bold" aria-hidden="true" /> Cancelar
              </button>
            </div>
          )}
          <div className="rounded-3xl border border-white/70 bg-white/45 p-2">
            <Textarea
              value={content}
              onChange={(event) => setContent(event.target.value)}
              placeholder="Escribe un comentario o una duda…"
              maxLength={2000}
              aria-label="Escribir comentario"
              className="min-h-28 border-transparent bg-transparent shadow-none focus:border-transparent focus:bg-transparent focus:shadow-none"
            />
            <div className="flex items-center justify-between gap-3 px-2 pb-1">
              <span className="text-[0.62rem] text-ink-faint">{content.length} / 2000</span>
              <Button
                type="submit"
                loading={createMutation.isPending}
                disabled={!content.trim()}
                className="min-h-9 px-4 py-2 text-xs"
              >
                Publicar
              </Button>
            </div>
          </div>
        </form>
      ) : (
        <div className="mt-6 flex flex-col items-start justify-between gap-4 rounded-3xl border border-white/70 bg-white/40 p-5 sm:flex-row sm:items-center">
          <div>
            <p className="text-sm font-semibold text-ink">Inicia sesión para comentar</p>
            <p className="mt-1 text-xs leading-5 text-ink-soft">
              Necesitas una cuenta de la USC.
            </p>
          </div>
          <Button to="/acceso" variant="ghost" className="shrink-0">
            Entrar
          </Button>
        </div>
      )}

      <div className="mt-4 divide-y divide-ink/8">
        {commentsQuery.isLoading ? (
          Array.from({ length: 3 }).map((_, index) => (
            <div key={index} className="flex gap-3 py-5">
              <Skeleton className="size-8 rounded-full" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-3 w-28" />
                <Skeleton className="h-4 w-full max-w-lg" />
                <Skeleton className="h-4 w-2/3 max-w-sm" />
              </div>
            </div>
          ))
        ) : commentsQuery.isError ? (
          <div className="py-10 text-center">
            <p className="text-sm text-ink-soft">No se pudo cargar la conversación.</p>
            <Button
              variant="ghost"
              className="mt-3"
              onClick={() => commentsQuery.refetch()}
            >
              Reintentar
            </Button>
          </div>
        ) : roots.length ? (
          roots.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              byParent={byParent}
              depth={0}
              fileId={fileId}
              onReply={(id) => {
                setParentId(id)
                document
                  .querySelector('textarea[aria-label="Escribir comentario"]')
                  ?.focus()
              }}
            />
          ))
        ) : (
          <div className="py-12 text-center">
            <span className="mx-auto grid size-11 place-items-center rounded-full bg-white/60 text-aqua-700">
              <LockKey size={20} weight="duotone" aria-hidden="true" />
            </span>
            <p className="mt-3 text-sm font-semibold text-ink">No hay comentarios</p>
            <p className="mx-auto mt-1 max-w-sm text-xs leading-5 text-ink-soft">
              Puedes abrir el hilo con una duda o una corrección.
            </p>
          </div>
        )}
      </div>
    </section>
  )
}
