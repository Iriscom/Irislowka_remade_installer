package net.iristeam.storycore.client.renderer.entity;

import net.iristeam.storycore.client.renderer.entity.model.MdLoverseModel;
import net.iristeam.storycore.entity.custom.MdLoverseEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

import static net.iristeam.storycore.StoryCore.id;

public class MdLoverseRenderer extends GeoEntityRenderer<MdLoverseEntity> {

    protected ItemStack mainHandItem;
    protected ItemStack offhandItem;
    public MdLoverseRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new MdLoverseModel());
//        this.addRenderLayer(new ItemArmorGeoLayer<MdLoverseEntity>(this) {
//            @Nullable
//            protected ItemStack getArmorItemForBone(GeoBone bone, MdLoverseEntity animatable) {
//                ItemStack var10000;
//                switch (bone.getName()) {
//                    case "armorBipedLeftFoot":
//                    case "armorBipedRightFoot":
//                    case "armorBipedLeftFoot2":
//                    case "armorBipedRightFoot2":
//                        var10000 = this.bootsStack;
//                        break;
//                    case "armorBipedLeftLeg":
//                    case "armorBipedRightLeg":
//                    case "armorBipedLeftLeg2":
//                    case "armorBipedRightLeg2":
//                        var10000 = this.leggingsStack;
//                        break;
//                    case "armorBipedBody":
//                    case "armorBipedRightArm":
//                    case "armorBipedLeftArm":
//                        var10000 = this.chestplateStack;
//                        break;
//                    case "armorBipedHead":
//                        var10000 = this.helmetStack;
//                        break;
//                    default:
//                        var10000 = null;
//                }
//
//                return var10000;
//            }
//
//            @Nonnull
//            protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, MdLoverseEntity animatable) {
//                EquipmentSlot var10000;
//                switch (bone.getName()) {
//                    case "armorBipedLeftFoot":
//                    case "armorBipedRightFoot":
//                    case "armorBipedLeftFoot2":
//                    case "armorBipedRightFoot2":
//                        var10000 = EquipmentSlot.FEET;
//                        break;
//                    case "armorBipedLeftLeg":
//                    case "armorBipedRightLeg":
//                    case "armorBipedLeftLeg2":
//                    case "armorBipedRightLeg2":
//                        var10000 = EquipmentSlot.LEGS;
//                        break;
//                    case "armorBipedRightArm":
//                        var10000 = !animatable.isLeftHanded() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
//                        break;
//                    case "armorBipedLeftArm":
//                        var10000 = animatable.isLeftHanded() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
//                        break;
//                    case "armorBipedBody":
//                        var10000 = EquipmentSlot.CHEST;
//                        break;
//                    case "armorBipedHead":
//                        var10000 = EquipmentSlot.HEAD;
//                        break;
//                    default:
//                        var10000 = super.getEquipmentSlotForBone(bone, stack, animatable);
//                }
//
//                return var10000;
//            }
//
//            @Nonnull
//            protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, MdLoverseEntity animatable, BipedEntityModel<?> baseModel) {
//                ModelPart var10000;
//                switch (bone.getName()) {
//                    case "armorBipedLeftFoot":
//                    case "armorBipedLeftFoot2":
//                    case "armorBipedLeftLeg":
//                    case "armorBipedLeftLeg2":
//                        var10000 = baseModel.leftLeg;
//                        break;
//                    case "armorBipedRightFoot":
//                    case "armorBipedRightFoot2":
//                    case "armorBipedRightLeg":
//                    case "armorBipedRightLeg2":
//                        var10000 = baseModel.rightLeg;
//                        break;
//                    case "armorBipedRightArm":
//                        var10000 = baseModel.rightArm;
//                        break;
//                    case "armorBipedLeftArm":
//                        var10000 = baseModel.leftArm;
//                        break;
//                    case "armorBipedBody":
//                        var10000 = baseModel.body;
//                        break;
//                    case "armorBipedHead":
//                        var10000 = baseModel.head;
//                        break;
//                    default:
//                        var10000 = super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
//                }
//
//                return var10000;
//            }
//        });
        this.addRenderLayer(new BlockAndItemGeoLayer<MdLoverseEntity>(this) {
            @Nullable
            protected ItemStack getStackForBone(GeoBone bone, MdLoverseEntity animatable) {
                ItemStack var10000;
                switch (bone.getName()) {
                    case "LeftArmBend" -> var10000 = animatable.isLeftHanded() ? MdLoverseRenderer.this.mainHandItem : MdLoverseRenderer.this.offhandItem;
                    case "RightArmBend" -> var10000 = animatable.isLeftHanded() ? MdLoverseRenderer.this.offhandItem : MdLoverseRenderer.this.mainHandItem;
                    default -> var10000 = null;
                }

                return var10000;
            }

            protected ModelTransformationMode getTransformTypeForStack(GeoBone bone, ItemStack stack, MdLoverseEntity animatable) {
                ModelTransformationMode var10000;
                switch (bone.getName()) {
                    case "LeftArmBend":
                    case "RightArmBend":
                        var10000 = ModelTransformationMode.THIRD_PERSON_RIGHT_HAND;
                        break;
                    default:
                        var10000 = ModelTransformationMode.NONE;
                }

                return var10000;
            }

            protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, MdLoverseEntity animatable, VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay) {
                if (stack == MdLoverseRenderer.this.mainHandItem) {
                    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                    poseStack.translate(0.0, 0.0, -0.3);
                    if (stack.getItem() instanceof ShieldItem) {
                        poseStack.translate(0.0, 0.125, -0.25);
                    }
                } else if (stack == MdLoverseRenderer.this.offhandItem) {
                    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
                    poseStack.translate(0.0, 0.0, -0.3);
                    if (stack.getItem() instanceof ShieldItem) {
                        poseStack.translate(0.0, 0.125, 0.25);
                        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
                    }
                }

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(MdLoverseEntity animatable) {
        return id("textures/entity/mdloverse.png");
    }

    @Override
    public void render(MdLoverseEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight) {
        if(entity.isBaby()) {
            poseStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
    public void preRender(MatrixStack poseStack, MdLoverseEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        this.mainHandItem = animatable.getMainHandStack();
        this.offhandItem = animatable.getOffHandStack();
    }
}