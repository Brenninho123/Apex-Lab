package com.apexlab.game.scene

import com.apexlab.game.theme.LabPalette
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable

class LabScene : Disposable {

    interface Listener {
        fun onHoverChanged(sample: LabSample?) {}
        fun onSampleAnalyzed(sample: LabSample) {}
    }

    private val layout = listOf(
        SampleType.ERLENMEYER_FLASK to -2.3f,
        SampleType.TEST_TUBE_RACK to -1.15f,
        SampleType.BEAKER to 0f,
        SampleType.ROUND_FLASK to 1.15f,
        SampleType.ERLENMEYER_FLASK to 2.3f
    )

    val orbit = OrbitCamera()
    var listener: Listener? = null

    private val environment = Environment()
    private val modelBatch = ModelBatch()
    private val disposables = mutableListOf<Disposable>()
    private val props = mutableListOf<ModelInstance>()
    private val sampleInstances = mutableListOf<ModelInstance>()
    private val sampleList = mutableListOf<LabSample>()
    private val centerScratch = Vector3()
    private var time = 0f

    val samples: List<LabSample> get() = sampleList
    var hovered: LabSample? = null
        private set

    val analyzedCount: Int get() = sampleList.count { it.analyzed }
    val totalCount: Int get() = sampleList.size
    val isComplete: Boolean get() = sampleList.isNotEmpty() && analyzedCount == totalCount

    init {
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.38f, 0.42f, 0.46f, 1f))
        environment.add(DirectionalLight().set(0.74f, 0.72f, 0.68f, -0.35f, -1f, -0.75f))
        environment.add(DirectionalLight().set(0.22f, 0.30f, 0.38f, 0.8f, -0.3f, 0.4f))

        val floor = LabTextures.floorTiles()
        val wall = LabTextures.wallTiles()
        val poster = LabTextures.periodicPoster()
        disposables += floor
        disposables += wall
        disposables += poster

        addProp(LabModels.room(floor, wall, poster), 0f, 0f, 0f)
        addProp(LabModels.bench(), 0f, 0f, 0f)
        addProp(LabModels.shelf(), 2.4f, 1.4f, -3.33f)
        addProp(LabModels.microscope(), -3.35f, 0f, -0.55f, 1.35f)

        layout.forEachIndexed { index, (type, x) ->
            addProp(LabModels.coaster(), x, 0f, 0f)

            val model = LabModels.sample(type, LabPalette.liquids[index % LabPalette.liquids.size])
            disposables += model
            val instance = ModelInstance(model)
            sampleInstances += instance
            sampleList += LabSample(type, instance, x, COASTER_HEIGHT, index * 1.3f)
        }
    }

    private fun addProp(model: Model, x: Float, y: Float, z: Float, scale: Float = 1f) {
        disposables += model
        val instance = ModelInstance(model)
        instance.transform.setToTranslation(x, y, z).scale(scale, scale, scale)
        props += instance
    }

    fun update(delta: Float) {
        time += delta
        sampleList.forEach { it.update(time, delta) }
        orbit.update()
    }

    fun render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        val sky = LabPalette.sky
        Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        modelBatch.begin(orbit.camera)
        modelBatch.render(props, environment)
        modelBatch.render(sampleInstances, environment)
        modelBatch.end()
    }

    fun resize(width: Int, height: Int) {
        orbit.resize(width, height)
    }

    fun pick(screenX: Int, screenY: Int): LabSample? {
        val ray = orbit.pickRay(screenX, screenY)
        var closest: LabSample? = null
        var closestDistance = Float.MAX_VALUE
        for (sample in sampleList) {
            if (sample.analyzed) continue
            if (!Intersector.intersectRayBoundsFast(ray, sample.bounds)) continue
            val distance = ray.origin.dst2(sample.instance.transform.getTranslation(centerScratch))
            if (distance < closestDistance) {
                closestDistance = distance
                closest = sample
            }
        }
        return closest
    }

    fun hover(screenX: Int, screenY: Int) {
        val target = pick(screenX, screenY)
        if (target === hovered) return
        hovered?.hovered = false
        target?.hovered = true
        hovered = target
        listener?.onHoverChanged(target)
    }

    fun analyzeAt(screenX: Int, screenY: Int): LabSample? {
        val sample = pick(screenX, screenY) ?: return null
        sample.markAnalyzed()
        if (hovered === sample) {
            hovered = null
            listener?.onHoverChanged(null)
        }
        listener?.onSampleAnalyzed(sample)
        return sample
    }

    override fun dispose() {
        modelBatch.dispose()
        disposables.forEach { it.dispose() }
        disposables.clear()
    }

    private companion object {
        const val COASTER_HEIGHT = 0.03f
    }
}
