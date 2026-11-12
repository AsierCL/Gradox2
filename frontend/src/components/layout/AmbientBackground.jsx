export function AmbientBackground() {
  return (
    <div aria-hidden="true" className="pointer-events-none fixed inset-0 -z-10 overflow-hidden">
      <div className="absolute inset-0 bg-[#edeff3]" />
      <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.48)_0%,transparent_30%,transparent_72%,rgba(255,255,255,0.34)_100%)]" />

      <div className="absolute -left-[20%] -top-[26%] size-[68vw] min-h-[34rem] min-w-[34rem] rounded-full bg-[radial-gradient(circle_at_42%_42%,rgba(255,255,255,0.95),rgba(255,255,255,0.3)_44%,transparent_72%)] blur-3xl md:animate-drift" />
      <div className="absolute -right-[16%] top-[4%] hidden size-[54vw] min-h-[28rem] min-w-[28rem] rounded-full bg-[radial-gradient(circle_at_50%_45%,rgba(210,215,224,0.8),rgba(255,255,255,0.22)_50%,transparent_74%)] blur-3xl md:block md:animate-drift md:[animation-delay:-9s]" />
      <div className="absolute bottom-[-22%] left-[22%] size-[58vw] min-h-[28rem] min-w-[28rem] rounded-full bg-[radial-gradient(circle_at_50%_50%,rgba(255,255,255,0.82),rgba(226,230,236,0.32)_48%,transparent_74%)] blur-3xl md:animate-drift md:[animation-delay:-16s]" />
      <div className="absolute left-[42%] top-[30%] hidden size-[34vw] min-h-[18rem] min-w-[18rem] rounded-full bg-[radial-gradient(circle_at_50%_50%,rgba(198,204,214,0.55),transparent_70%)] blur-3xl lg:block lg:animate-drift lg:[animation-delay:-22s]" />

      <div
        className="absolute inset-0 opacity-[0.02] mix-blend-multiply"
        style={{
          backgroundImage:
            "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 160 160' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.9'/%3E%3C/svg%3E\")",
          backgroundSize: '160px 160px',
        }}
      />
    </div>
  )
}
